package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.model.Resource;
import com.hospital.resourceallocation.model.ResourceHistory;
import com.hospital.resourceallocation.repository.ResourceHistoryRepository;
import com.hospital.resourceallocation.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Medical Resource Management Service
 * Manages reusable equipment, consumable supplies, and hospital facilities.
 */
@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceHistoryRepository historyRepository;
    private final com.hospital.resourceallocation.repository.BedRepository bedRepository;
    private final com.hospital.resourceallocation.repository.PatientRepository patientRepository;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository,
                           ResourceHistoryRepository historyRepository,
                           com.hospital.resourceallocation.repository.BedRepository bedRepository,
                           com.hospital.resourceallocation.repository.PatientRepository patientRepository) {
        this.resourceRepository = resourceRepository;
        this.historyRepository = historyRepository;
        this.bedRepository = bedRepository;
        this.patientRepository = patientRepository;
    }

    public List<com.hospital.resourceallocation.dto.UnifiedResourceItemDTO> getAllCombinedResources(String hospitalId) {
        String hid = hospitalId != null ? hospitalId : "H-001";
        List<com.hospital.resourceallocation.dto.UnifiedResourceItemDTO> list = new java.util.ArrayList<>();

        // 1. Add All Beds
        List<com.hospital.resourceallocation.model.Bed> beds = bedRepository.findByHospitalId(hid);
        for (com.hospital.resourceallocation.model.Bed b : beds) {
            com.hospital.resourceallocation.dto.UnifiedResourceItemDTO item = new com.hospital.resourceallocation.dto.UnifiedResourceItemDTO();
            item.setId(b.getBedId());
            item.setName("Bed " + b.getBedNumber() + " (" + b.getFloorWard() + ")");
            item.setCategory("BED");
            item.setSubType(b.getBedType());
            item.setStatus(b.getStatus()); // AVAILABLE or OCCUPIED
            item.setTotalUnits(1);
            item.setAvailableUnits("AVAILABLE".equalsIgnoreCase(b.getStatus()) ? 1 : 0);
            item.setAllocatedUnits("OCCUPIED".equalsIgnoreCase(b.getStatus()) ? 1 : 0);
            item.setAllocatedPatientId(b.getCurrentPatientId());
            if (b.getCurrentPatientId() != null) {
                patientRepository.findById(b.getCurrentPatientId())
                        .ifPresent(p -> item.setAllocatedPatientName(p.getFullName()));
            }
            item.setLocation(b.getFloorWard());
            list.add(item);
        }

        // 2. Add All Resources (Equipment, Supplies, Facilities)
        List<Resource> resources = resourceRepository.findByHospitalId(hid);
        for (Resource r : resources) {
            com.hospital.resourceallocation.dto.UnifiedResourceItemDTO item = new com.hospital.resourceallocation.dto.UnifiedResourceItemDTO();
            item.setId(r.getResourceId());
            item.setName(r.getResourceName());
            item.setCategory(r.getCategory());
            item.setSubType(Boolean.TRUE.equals(r.getIsReusable()) ? "Reusable" : "Consumable");
            item.setStatus(r.getAvailableQuantity() > 0 ? "AVAILABLE" : "OCCUPIED");
            item.setTotalUnits(r.getTotalQuantity());
            item.setAvailableUnits(r.getAvailableQuantity());
            item.setAllocatedUnits(r.getAllocatedQuantity());
            item.setLocation("Hospital Inventory (" + hid + ")");
            list.add(item);
        }

        return list;
    }

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public List<Resource> getResourcesByHospital(String hospitalId) {
        return resourceRepository.findByHospitalId(hospitalId);
    }

    public List<Resource> getResourcesByCategory(String hospitalId, String category) {
        return resourceRepository.findByHospitalIdAndCategory(hospitalId, category);
    }

    public Optional<Resource> getResourceById(String resourceId) {
        return resourceRepository.findById(resourceId);
    }

    @Transactional
    public Resource allocateResource(String resourceId, int quantity, String patientId, String visitId) {
        Resource resource = resourceRepository.findByIdForUpdate(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        resource.allocateUnits(quantity);
        Resource saved = resourceRepository.save(resource);

        // Audit log
        historyRepository.save(new ResourceHistory(
                resourceId, resource.getResourceName(), "ALLOCATED", quantity,
                patientId, visitId, "Allocated to patient"
        ));

        return saved;
    }

    @Transactional
    public Resource releaseResource(String resourceId, int quantity, String patientId, String visitId) {
        Resource resource = resourceRepository.findByIdForUpdate(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        resource.releaseUnits(quantity);
        Resource saved = resourceRepository.save(resource);

        String action = Boolean.TRUE.equals(resource.getIsReusable()) ? "RELEASED" : "CONSUMED";
        historyRepository.save(new ResourceHistory(
                resourceId, resource.getResourceName(), action, quantity,
                patientId, visitId, "Released after treatment/discharge"
        ));

        return saved;
    }

    @Transactional
    public Resource addResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    public List<ResourceHistory> getAllResourceHistory() {
        return historyRepository.findAllByOrderByTimestampDesc();
    }

    public List<ResourceHistory> getHistoryByVisitId(String visitId) {
        return historyRepository.findByVisitIdOrderByTimestampDesc(visitId);
    }
}
