package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.dto.ResourceAllocationRequestDTO;
import com.hospital.resourceallocation.model.*;
import com.hospital.resourceallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Centralized Resource Allocation Engine
 * Orchestrates multi-resource atomic reservations, transactional rollback on partial unavailability,
 * pessimistic row locking, and waiting queue routing.
 */
@Service
public class ResourceAllocationEngineService implements ResourceAllocationPolicy {

    private final BedRepository bedRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceHistoryRepository historyRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final WaitingQueueService waitingQueueService;

    @Autowired
    public ResourceAllocationEngineService(BedRepository bedRepository,
                                          ResourceRepository resourceRepository,
                                          ResourceAllocationRepository allocationRepository,
                                          ResourceHistoryRepository historyRepository,
                                          PatientVisitRepository patientVisitRepository,
                                          WaitingQueueService waitingQueueService) {
        this.bedRepository = bedRepository;
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.historyRepository = historyRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.waitingQueueService = waitingQueueService;
    }

    @Override
    public boolean validateAvailability(List<String> requiredResources, Hospital hospital) {
        if (requiredResources == null || requiredResources.isEmpty()) return true;
        for (String resName : requiredResources) {
            Optional<Resource> resOpt = resourceRepository.findByHospitalIdAndResourceNameIgnoreCase(hospital.getHospitalId(), resName);
            if (resOpt.isEmpty() || !resOpt.get().checkAvailability(1)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean supportsRequirement(String resourceType, String patientCondition) {
        return resourceType != null && !resourceType.trim().isEmpty();
    }

    /**
     * Atomically reserves all mandatory resources together for a patient visit.
     * Prevents partial allocation; rolls back cleanly if any resource is unavailable.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<ResourceAllocation> allocateResources(ResourceAllocationRequestDTO dto) {
        PatientVisit visit = patientVisitRepository.findById(dto.getVisitId())
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + dto.getVisitId()));

        String hospitalId = "H-001";
        List<Resource> lockedResources = new ArrayList<>();
        List<ResourceAllocation> completedAllocations = new ArrayList<>();

        // 1. Bed allocation if requested
        if (dto.getBedId() != null && !dto.getBedId().trim().isEmpty()) {
            Bed bed = bedRepository.findByIdForUpdate(dto.getBedId())
                    .orElseThrow(() -> new IllegalArgumentException("Bed not found: " + dto.getBedId()));
            if (!bed.checkAvailability()) {
                // If requested bed is unavailable, enroll in Resource Waiting Queue
                waitingQueueService.enqueueResourceRequest(
                        dto.getVisitId(), visit.getPatientId(), visit.getSeverityLevel(),
                        bed.getBedType(), "General Medicine", "Bed: " + bed.getBedNumber()
                );
                throw new IllegalStateException("Bed " + bed.getBedNumber() + " is currently unavailable. Request enrolled in Resource Waiting Queue.");
            }
            bed.allocate(visit.getPatientId(), dto.getVisitId());
            bedRepository.save(bed);
            visit.setAssignedBedId(bed.getBedId());
        }

        // 2. Multi-resource atomic lock check
        if (dto.getResourceNames() != null && !dto.getResourceNames().isEmpty()) {
            for (String resName : dto.getResourceNames()) {
                Resource res = resourceRepository.findByHospitalIdAndResourceNameIgnoreCase(hospitalId, resName)
                        .orElse(null);

                if (res == null) {
                    throw new IllegalArgumentException("Resource not recognized: " + resName);
                }

                // Acquire pessimistic lock on resource row
                Resource lockedRes = resourceRepository.findByIdForUpdate(res.getResourceId())
                        .orElseThrow(() -> new IllegalStateException("Failed to lock resource: " + resName));

                int requestedQty = dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1;
                if (!lockedRes.checkAvailability(requestedQty)) {
                    // One mandatory resource is unavailable! Refuse partial allocation and queue
                    waitingQueueService.enqueueResourceRequest(
                            dto.getVisitId(), visit.getPatientId(), visit.getSeverityLevel(),
                            null, null, String.join(", ", dto.getResourceNames())
                    );
                    throw new IllegalStateException("Mandatory resource '" + resName + "' is unavailable. All partial allocations cancelled. Patient placed in Resource Waiting Queue.");
                }
                lockedResources.add(lockedRes);
            }

            // All mandatory resources are verified available; now commit reservations
            for (Resource res : lockedResources) {
                int qty = dto.getQuantity() != null && dto.getQuantity() > 0 ? dto.getQuantity() : 1;
                res.allocateUnits(qty);
                resourceRepository.save(res);

                long count = allocationRepository.count();
                String allocId = String.format("ALL-%04d", 7001 + count);
                while (allocationRepository.existsById(allocId)) {
                    count++;
                    allocId = String.format("ALL-%04d", 7001 + count);
                }

                ResourceAllocation allocation = new ResourceAllocation(
                        allocId, dto.getVisitId(), visit.getPatientId(), res.getResourceId(),
                        res.getResourceName(), res.getCategory(), qty
                );
                completedAllocations.add(allocationRepository.save(allocation));

                historyRepository.save(new ResourceHistory(
                        res.getResourceId(), res.getResourceName(), "ALLOCATED", qty,
                        visit.getPatientId(), dto.getVisitId(), "Allocated by Resource Engine"
                ));
            }
        }

        patientVisitRepository.save(visit);
        return completedAllocations;
    }

    public List<ResourceAllocation> getActiveAllocations(String visitId) {
        return allocationRepository.findByVisitIdAndStatus(visitId, "ACTIVE");
    }

    public List<ResourceAllocation> getAllAllocations() {
        return allocationRepository.findAll();
    }
}
