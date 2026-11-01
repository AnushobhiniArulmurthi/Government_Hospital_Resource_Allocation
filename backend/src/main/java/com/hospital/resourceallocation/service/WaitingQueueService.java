package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.algorithms.HospitalPriorityQueue;
import com.hospital.resourceallocation.model.*;
import com.hospital.resourceallocation.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Waiting Queue Service
 * Bridges in-memory Java PriorityQueue structures with MySQL persistent queue tables.
 * Maintains two separate priority queues:
 * 1. Doctor Consultation Waiting Queue
 * 2. Resource Allocation Waiting Queue
 */
@Service
public class WaitingQueueService {

    private final HospitalPriorityQueue priorityQueue;
    private final ConsultationQueueRepository consultationQueueRepository;
    private final ResourceQueueRepository resourceQueueRepository;
    private final DoctorRepository doctorRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final BedRepository bedRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceHistoryRepository historyRepository;
    private final AdmissionRepository admissionRepository;

    @Autowired
    public WaitingQueueService(HospitalPriorityQueue priorityQueue,
                               ConsultationQueueRepository consultationQueueRepository,
                               ResourceQueueRepository resourceQueueRepository,
                               DoctorRepository doctorRepository,
                               PatientVisitRepository patientVisitRepository,
                               BedRepository bedRepository,
                               ResourceRepository resourceRepository,
                               ResourceAllocationRepository allocationRepository,
                               ResourceHistoryRepository historyRepository,
                               AdmissionRepository admissionRepository) {
        this.priorityQueue = priorityQueue;
        this.consultationQueueRepository = consultationQueueRepository;
        this.resourceQueueRepository = resourceQueueRepository;
        this.doctorRepository = doctorRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.bedRepository = bedRepository;
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.historyRepository = historyRepository;
        this.admissionRepository = admissionRepository;
    }

    /**
     * Initializes in-memory PriorityQueues from persistent MySQL queue records on application boot.
     */
    @PostConstruct
    public void loadQueuesFromDatabase() {
        priorityQueue.clearAll();
        List<ConsultationWaitingQueueItem> pendingConsultations =
                consultationQueueRepository.findByStatusOrderByPriorityScoreAscArrivalTimeAsc("WAITING");
        for (ConsultationWaitingQueueItem item : pendingConsultations) {
            priorityQueue.addConsultationRequest(item);
        }

        List<ResourceWaitingQueueItem> pendingResources =
                resourceQueueRepository.findByStatusOrderByPriorityScoreAscArrivalTimeAsc("WAITING");
        for (ResourceWaitingQueueItem item : pendingResources) {
            priorityQueue.addResourceRequest(item);
        }
    }

    // --- Consultation Queue Methods ---

    @Transactional
    public void enqueueConsultation(String visitId, String patientId, String severityLevel, String requiredSpecialization) {
        long count = consultationQueueRepository.count();
        String queueId = String.format("CWQ-%04d", 9001 + count);
        while (consultationQueueRepository.existsById(queueId)) {
            count++;
            queueId = String.format("CWQ-%04d", 9001 + count);
        }

        ConsultationWaitingQueueItem item = new ConsultationWaitingQueueItem(
                queueId, visitId, patientId, severityLevel, requiredSpecialization, LocalDateTime.now()
        );
        consultationQueueRepository.save(item);
        priorityQueue.addConsultationRequest(item);
    }

    public List<ConsultationWaitingQueueItem> getConsultationWaitingQueue() {
        return priorityQueue.getAllConsultationRequests();
    }

    /**
     * Re-evaluates Consultation Waiting Queue whenever a doctor becomes available.
     */
    @Transactional
    public void processConsultationQueue() {
        List<Doctor> availableDoctors = doctorRepository.findByHospitalIdAndDutyStatusAndAvailabilityStatus("H-001", "ON_DUTY", "AVAILABLE");
        if (availableDoctors.isEmpty()) return;

        List<ConsultationWaitingQueueItem> items = priorityQueue.getAllConsultationRequests();
        for (ConsultationWaitingQueueItem item : items) {
            for (Doctor doc : availableDoctors) {
                if (doc.canAcceptConsultation() && doc.matchesSpecialization(item.getRequiredSpecialization())) {
                    // Match found! Assign doctor
                    doc.allocate(item.getPatientId(), item.getVisitId());
                    doctorRepository.save(doc);

                    PatientVisit visit = patientVisitRepository.findById(item.getVisitId()).orElse(null);
                    if (visit != null) {
                        visit.setAssignedDoctorId(doc.getDoctorId());
                        visit.setVisitStatus("IN_CONSULTATION");
                        patientVisitRepository.save(visit);
                    }

                    item.setStatus("ASSIGNED");
                    consultationQueueRepository.save(item);
                    priorityQueue.removeConsultationRequest(item.getQueueId());
                    break;
                }
            }
        }
    }

    // --- Resource Queue Methods ---

    @Transactional
    public void enqueueResourceRequest(String visitId, String patientId, String severityLevel,
                                       String requiredBedType, String requiredSpecialization, String requiredResources) {
        long count = resourceQueueRepository.count();
        String queueId = String.format("RWQ-%04d", 9101 + count);
        while (resourceQueueRepository.existsById(queueId)) {
            count++;
            queueId = String.format("RWQ-%04d", 9101 + count);
        }

        ResourceWaitingQueueItem item = new ResourceWaitingQueueItem(
                queueId, visitId, patientId, severityLevel, requiredBedType, requiredSpecialization,
                requiredResources, LocalDateTime.now()
        );
        resourceQueueRepository.save(item);
        priorityQueue.addResourceRequest(item);
    }

    public List<ResourceWaitingQueueItem> getResourceWaitingQueue() {
        return priorityQueue.getAllResourceRequests();
    }

    /**
     * Re-evaluates Resource Allocation Waiting Queue whenever a bed, equipment, or facility is released.
     * Automatically allocates freed resources to the highest-priority waiting patient without manual intervention.
     */
    @Transactional
    public void processResourceQueue() {
        List<ResourceWaitingQueueItem> items = priorityQueue.getAllResourceRequests();
        if (items.isEmpty()) return;

        for (ResourceWaitingQueueItem item : items) {
            if (!"WAITING".equalsIgnoreCase(item.getStatus()) && !"READY_FOR_ALLOCATION".equalsIgnoreCase(item.getStatus())) {
                continue;
            }

            Bed allocatedBed = null;
            boolean bedRequired = item.getRequiredBedType() != null && !"NO_BED".equalsIgnoreCase(item.getRequiredBedType());

            if (bedRequired) {
                List<Bed> availableBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus("H-001", item.getRequiredBedType(), "AVAILABLE");
                if (!availableBeds.isEmpty()) {
                    allocatedBed = availableBeds.get(0);
                }
            }

            // If a bed was required but none available, continue to next waiting patient
            if (bedRequired && allocatedBed == null) {
                continue;
            }

            PatientVisit visit = patientVisitRepository.findById(item.getVisitId()).orElse(null);
            if (visit == null) continue;

            // Perform automatic bed allocation
            if (allocatedBed != null) {
                allocatedBed.allocate(item.getPatientId(), item.getVisitId());
                bedRepository.save(allocatedBed);

                visit.setAssignedBedId(allocatedBed.getBedId());
                visit.setVisitStatus("ADMITTED");
                visit.setPatientCategory("Inpatient");
                patientVisitRepository.save(visit);

                // Auto-create admission record
                long admCount = admissionRepository.count();
                String admissionId = String.format("ADM-%04d", 6001 + admCount);
                while (admissionRepository.existsById(admissionId)) {
                    admCount++;
                    admissionId = String.format("ADM-%04d", 6001 + admCount);
                }
                Admission admission = new Admission(
                        admissionId, item.getVisitId(), item.getPatientId(),
                        visit.getAssignedDoctorId() != null ? visit.getAssignedDoctorId() : "D-001",
                        "H-001", allocatedBed.getBedId(), "Auto-admitted from Resource Priority Queue as bed became available"
                );
                admissionRepository.save(admission);
            }

            // Allocate any required resources if available
            if (item.getRequiredResources() != null && !item.getRequiredResources().isEmpty()) {
                String[] resNames = item.getRequiredResources().split(",");
                for (String resName : resNames) {
                    String cleanName = resName.trim();
                    if (cleanName.isEmpty() || cleanName.equalsIgnoreCase("All")) continue;
                    resourceRepository.findByHospitalIdAndResourceNameIgnoreCase("H-001", cleanName).ifPresent(res -> {
                        if (res.checkAvailability(1)) {
                            res.allocateUnits(1);
                            resourceRepository.save(res);

                            long allocCount = allocationRepository.count();
                            String allocId = String.format("ALL-%04d", 7001 + allocCount);
                            while (allocationRepository.existsById(allocId)) {
                                allocCount++;
                                allocId = String.format("ALL-%04d", 7001 + allocCount);
                            }
                            ResourceAllocation alloc = new ResourceAllocation(
                                    allocId, item.getVisitId(), item.getPatientId(), res.getResourceId(),
                                    res.getResourceName(), res.getCategory(), 1
                            );
                            allocationRepository.save(alloc);

                            historyRepository.save(new ResourceHistory(
                                    res.getResourceId(), res.getResourceName(), "ALLOCATED", 1,
                                    item.getPatientId(), item.getVisitId(), "Auto-allocated from waiting queue"
                            ));
                        }
                    });
                }
            }

            // Mark queue item as ALLOCATED and remove from active priority queue
            item.setStatus("ALLOCATED");
            resourceQueueRepository.save(item);
            priorityQueue.removeResourceRequest(item.getQueueId());
        }
    }
}
