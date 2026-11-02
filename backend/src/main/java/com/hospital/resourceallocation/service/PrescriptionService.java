package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.algorithms.DijkstraRouter;
import com.hospital.resourceallocation.algorithms.RouteResult;
import com.hospital.resourceallocation.dto.PrescriptionRequestDTO;
import com.hospital.resourceallocation.dto.PrescriptionResponseDTO;
import com.hospital.resourceallocation.model.*;
import com.hospital.resourceallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Doctor Resource Prescription Service
 * Fully automates hospital resource prescription, availability verification,
 * resource locking/allocation, inter-hospital referral routing, and priority queue fallback.
 */
@Service
public class PrescriptionService {

    private final PatientRepository patientRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceHistoryRepository historyRepository;
    private final AdmissionRepository admissionRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final DiagnosticServiceRepository diagnosticServiceRepository;
    private final HospitalRepository hospitalRepository;
    private final ReferralRepository referralRepository;
    private final WaitingQueueService waitingQueueService;
    private final DijkstraRouter routePlanner;

    @Autowired
    public PrescriptionService(PatientRepository patientRepository,
                               PatientVisitRepository patientVisitRepository,
                               DoctorRepository doctorRepository,
                               BedRepository bedRepository,
                               ResourceRepository resourceRepository,
                               ResourceAllocationRepository allocationRepository,
                               ResourceHistoryRepository historyRepository,
                               AdmissionRepository admissionRepository,
                               DiagnosticOrderRepository diagnosticOrderRepository,
                               DiagnosticServiceRepository diagnosticServiceRepository,
                               HospitalRepository hospitalRepository,
                               ReferralRepository referralRepository,
                               WaitingQueueService waitingQueueService,
                               DijkstraRouter routePlanner) {
        this.patientRepository = patientRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.historyRepository = historyRepository;
        this.admissionRepository = admissionRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.diagnosticServiceRepository = diagnosticServiceRepository;
        this.hospitalRepository = hospitalRepository;
        this.referralRepository = referralRepository;
        this.waitingQueueService = waitingQueueService;
        this.routePlanner = routePlanner;
    }

    /**
     * Executes the complete doctor prescription workflow:
     * 1. Validates patient & active visit.
     * 2. Checks resource availability in primary hospital (H-001).
     * 3. If available: Allocates resources, reserves bed, and admits patient.
     * 4. If unavailable: Automatically searches referral hospitals via Dijkstra.
     * 5. If no referral hospital has resources: Enqueues into Severity-Based Priority Queue.
     */
    @Transactional
    public PrescriptionResponseDTO processPrescription(PrescriptionRequestDTO dto) {
        // 1. Locate patient
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + dto.getPatientId()));

        // Locate or create active visit
        List<PatientVisit> visits = patientVisitRepository.findByPatientIdOrderByArrivalTimeDesc(patient.getPatientId());
        PatientVisit visit;
        if (visits.isEmpty() || "DISCHARGED".equalsIgnoreCase(visits.get(0).getVisitStatus())
                || "COMPLETED".equalsIgnoreCase(visits.get(0).getVisitStatus())) {
            long visitCount = patientVisitRepository.count();
            String visitId = String.format("VIS-%04d", 2001 + visitCount);
            while (patientVisitRepository.existsById(visitId)) {
                visitCount++;
                visitId = String.format("VIS-%04d", 2001 + visitCount);
            }
            visit = new PatientVisit(visitId, patient.getPatientId(),
                    dto.getSeverity() != null ? dto.getSeverity() : "Normal",
                    "Outpatient", LocalDateTime.now(), dto.getNotes() != null ? dto.getNotes() : "Doctor Consultation");
            visit.setVisitStatus("IN_CONSULTATION");
        } else {
            visit = visits.get(0);
        }

        // Update severity and doctor notes if provided
        if (dto.getSeverity() != null && !dto.getSeverity().trim().isEmpty()) {
            visit.setSeverityLevel(dto.getSeverity().trim());
        }
        if (dto.getNotes() != null && !dto.getNotes().trim().isEmpty()) {
            visit.setNotes(dto.getNotes().trim());
        }

        String doctorId = dto.getDoctorId() != null && !dto.getDoctorId().trim().isEmpty() ?
                dto.getDoctorId() : (visit.getAssignedDoctorId() != null ? visit.getAssignedDoctorId() : "D-001");
        visit.setAssignedDoctorId(doctorId);

        PrescriptionResponseDTO response = new PrescriptionResponseDTO();
        response.setPatientId(patient.getPatientId());
        response.setPatientName(patient.getFullName());
        response.setVisitId(visit.getVisitId());
        response.setSeverity(visit.getSeverityLevel());

        // 2. Resource Availability Check in H-001 (Primary Hospital)
        List<String> missingResources = new ArrayList<>();
        Bed availableBed = null;

        // Bed Check
        boolean requiresBed = Boolean.TRUE.equals(dto.getAdmissionRequired()) ||
                (dto.getBedType() != null && !dto.getBedType().trim().isEmpty() && !"NONE".equalsIgnoreCase(dto.getBedType()) && !"NO_BED".equalsIgnoreCase(dto.getBedType()));

        String requestedBedType = dto.getBedType() != null && !dto.getBedType().trim().isEmpty() && !"NONE".equalsIgnoreCase(dto.getBedType()) ?
                dto.getBedType().toUpperCase() : (requiresBed ? "GENERAL" : null);

        if (requiresBed && requestedBedType != null) {
            List<Bed> freeBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus("H-001", requestedBedType, "AVAILABLE");
            if (!freeBeds.isEmpty()) {
                availableBed = freeBeds.get(0);
            } else {
                missingResources.add("Bed (" + requestedBedType + ")");
            }
        }

        // Diagnostic Scans / Services Check
        List<DiagnosticService> localServices = diagnosticServiceRepository.findByHospitalIdAndIsAvailableTrue("H-001");
        List<String> validLocalServices = new ArrayList<>();
        if (dto.getServices() != null) {
            for (String srv : dto.getServices()) {
                String cleanSrv = srv.trim().toUpperCase();
                boolean isLocal = localServices.stream()
                        .anyMatch(ds -> ds.getTestType().equalsIgnoreCase(cleanSrv) || ds.getServiceCode().equalsIgnoreCase(cleanSrv));
                if (isLocal) {
                    validLocalServices.add(cleanSrv);
                } else {
                    missingResources.add(cleanSrv.replace("_", " "));
                }
            }
        }

        // Medical Equipment / Treatments Check
        List<Resource> availableEquipments = new ArrayList<>();
        if (dto.getTreatments() != null) {
            for (String trt : dto.getTreatments()) {
                String cleanTrt = trt.trim();
                if (cleanTrt.isEmpty()) continue;
                Optional<Resource> resOpt = resourceRepository.findByHospitalIdAndResourceNameIgnoreCase("H-001", cleanTrt);
                if (resOpt.isPresent() && resOpt.get().checkAvailability(1)) {
                    availableEquipments.add(resOpt.get());
                } else {
                    missingResources.add(cleanTrt);
                }
            }
        }

        // =========================================================================
        // CASE A: ALL PRESCRIBED RESOURCES ARE AVAILABLE LOCALLY
        // =========================================================================
        if (missingResources.isEmpty()) {
            // Allocate Bed if requested
            if (availableBed != null) {
                availableBed.allocate(patient.getPatientId(), visit.getVisitId());
                bedRepository.save(availableBed);

                visit.setAssignedBedId(availableBed.getBedId());
                visit.setVisitStatus("ADMITTED");
                visit.setPatientCategory("Inpatient");

                // Create Admission record
                long admCount = admissionRepository.count();
                String admissionId = String.format("ADM-%04d", 6001 + admCount);
                while (admissionRepository.existsById(admissionId)) {
                    admCount++;
                    admissionId = String.format("ADM-%04d", 6001 + admCount);
                }
                Admission admission = new Admission(
                        admissionId, visit.getVisitId(), patient.getPatientId(), doctorId,
                        "H-001", availableBed.getBedId(), "Admitted via Doctor Prescription: " + (dto.getNotes() != null ? dto.getNotes() : "Routine Admission")
                );
                admissionRepository.save(admission);

                // Update doctor inpatient caseload
                doctorRepository.findById(doctorId).ifPresent(doc -> {
                    doc.assignInpatient();
                    doctorRepository.save(doc);
                });

                response.setAllocatedBedNumber(availableBed.getBedNumber());
                response.setAllocatedBedType(availableBed.getBedType());
                response.setAdmissionId(admissionId);
            } else if (visit.getVisitStatus().equals("IN_CONSULTATION") || visit.getVisitStatus().equals("REGISTERED")) {
                visit.setVisitStatus("TREATMENT_PLANNED");
            }

            // Allocate Diagnostic Orders
            List<String> ordersCreated = new ArrayList<>();
            for (String srvType : validLocalServices) {
                long doCount = diagnosticOrderRepository.count();
                String orderId = String.format("DO-%04d", 5001 + doCount);
                while (diagnosticOrderRepository.existsById(orderId)) {
                    doCount++;
                    orderId = String.format("DO-%04d", 5001 + doCount);
                }
                DiagnosticOrder order = new DiagnosticOrder(
                        orderId, visit.getVisitId(), patient.getPatientId(), doctorId,
                        srvType, visit.getSeverityLevel(), null
                );
                diagnosticOrderRepository.save(order);
                ordersCreated.add(srvType + " (" + orderId + ")");
            }
            response.setDiagnosticOrders(ordersCreated);

            // Allocate Equipment
            List<String> allocatedResList = new ArrayList<>();
            for (Resource res : availableEquipments) {
                res.allocateUnits(1);
                resourceRepository.save(res);

                long allCount = allocationRepository.count();
                String allocId = String.format("ALL-%04d", 7001 + allCount);
                while (allocationRepository.existsById(allocId)) {
                    allCount++;
                    allocId = String.format("ALL-%04d", 7001 + allCount);
                }
                ResourceAllocation alloc = new ResourceAllocation(
                        allocId, visit.getVisitId(), patient.getPatientId(), res.getResourceId(),
                        res.getResourceName(), res.getCategory(), 1
                );
                allocationRepository.save(alloc);

                historyRepository.save(new ResourceHistory(
                        res.getResourceId(), res.getResourceName(), "ALLOCATED", 1,
                        patient.getPatientId(), visit.getVisitId(), "Allocated & locked via Doctor Prescription"
                ));
                allocatedResList.add(res.getResourceName());
            }
            response.setAllocatedResources(allocatedResList);

            patientVisitRepository.save(visit);

            response.setStatus("ALLOCATED");
            response.setMessage("All prescribed resources successfully verified, allocated, and locked for " + patient.getFullName() + ".");
            return response;
        }

        // =========================================================================
        // CASE B: RESOURCES UNAVAILABLE LOCALLY -> INITIATE REFERRAL VIA DIJKSTRA
        // =========================================================================
        Hospital primaryHospital = hospitalRepository.findById("H-001").orElse(null);
        String originLocation = primaryHospital != null ? primaryHospital.getLocationName() : "Nalam Central";
        List<Hospital> referralCandidates = hospitalRepository.findByIsPrimaryFalse();

        Hospital bestReferralHospital = null;
        RouteResult bestRoute = null;
        int minTravelTime = Integer.MAX_VALUE;

        for (Hospital candidate : referralCandidates) {
            String hid = candidate.getHospitalId();
            boolean satisfies = true;

            // Check if candidate hospital has the missing bed
            if (requiresBed && requestedBedType != null) {
                List<Bed> candBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus(hid, requestedBedType, "AVAILABLE");
                if (candBeds.isEmpty()) {
                    satisfies = false;
                }
            }

            // Check if candidate hospital has missing services (e.g. MRI, CT)
            if (satisfies && dto.getServices() != null) {
                List<DiagnosticService> candServices = diagnosticServiceRepository.findByHospitalIdAndIsAvailableTrue(hid);
                for (String srv : dto.getServices()) {
                    String cleanSrv = srv.trim().toUpperCase();
                    boolean hasSrv = candServices.stream()
                            .anyMatch(ds -> ds.getTestType().equalsIgnoreCase(cleanSrv) || ds.getServiceCode().equalsIgnoreCase(cleanSrv));
                    if (!hasSrv) {
                        satisfies = false;
                        break;
                    }
                }
            }

            if (satisfies) {
                RouteResult route = routePlanner.findShortestRoute(originLocation, candidate.getLocationName());
                if (route.getTotalTravelTimeMinutes() < minTravelTime) {
                    minTravelTime = route.getTotalTravelTimeMinutes();
                    bestReferralHospital = candidate;
                    bestRoute = route;
                }
            }
        }

        // If a matching referral hospital with required resources is found
        if (bestReferralHospital != null && bestRoute != null) {
            long refCount = referralRepository.count();
            String referralId = String.format("REF-%04d", 8001 + refCount);
            while (referralRepository.existsById(referralId)) {
                refCount++;
                referralId = String.format("REF-%04d", 8001 + refCount);
            }

            String reason = "Prescribed resources unavailable locally: " + String.join(", ", missingResources);
            Referral referral = new Referral(
                    referralId, visit.getVisitId(), patient.getPatientId(), "H-001",
                    bestReferralHospital.getHospitalId(), reason,
                    bestRoute.getTotalTravelTimeMinutes(), bestRoute.getFormattedPath()
            );
            referral.setStatus("PENDING_APPROVAL");
            referralRepository.save(referral);

            visit.setVisitStatus("REFERRAL_INITIATED");
            patientVisitRepository.save(visit);

            response.setStatus("REFERRAL_INITIATED");
            response.setMessage("Prescribed resources (" + String.join(", ", missingResources) +
                    ") are unavailable at primary hospital. Patient automatically transferred to Referral Hospitals Module for Administrator Verification & Approval.");
            response.setReferralId(referralId);
            response.setReferralHospitalId(bestReferralHospital.getHospitalId());
            response.setReferralHospitalName(bestReferralHospital.getHospitalName());
            response.setReferralHospitalContact(bestReferralHospital.getContactNumber());
            response.setReferralHospitalAddress(bestReferralHospital.getAddress());
            response.setReferralTravelTimeMinutes(bestRoute.getTotalTravelTimeMinutes());
            response.setReferralRoute(bestRoute.getFormattedPath());
            return response;
        }

        // =========================================================================
        // CASE C: NO REFERRAL HOSPITAL HAS RESOURCES -> ENQUEUE IN PRIORITY QUEUE
        // =========================================================================
        String requiredResSummary = String.join(", ", missingResources);
        waitingQueueService.enqueueResourceRequest(
                visit.getVisitId(), patient.getPatientId(), visit.getSeverityLevel(),
                requestedBedType, null, requiredResSummary
        );

        visit.setVisitStatus("WAITING_FOR_RESOURCE");
        patientVisitRepository.save(visit);

        int score = "Critical".equalsIgnoreCase(visit.getSeverityLevel()) ? 1 :
                ("Serious".equalsIgnoreCase(visit.getSeverityLevel()) ? 2 : 3);

        response.setStatus("WAITING_LIST");
        response.setMessage("Required resources (" + requiredResSummary +
                ") unavailable across the entire hospital network. Patient automatically prioritized into the Severity-Based Waiting Queue (" +
                visit.getSeverityLevel() + " - Priority Rank " + score + "). Newly released resources upon patient discharge will auto-allocate.");
        response.setPriorityScore(score);
        return response;
    }
}
