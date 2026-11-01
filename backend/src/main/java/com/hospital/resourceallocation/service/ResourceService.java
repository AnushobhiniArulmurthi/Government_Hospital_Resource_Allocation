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

    @Autowired
    public ResourceService(ResourceRepository resourceRepository,
                           ResourceHistoryRepository historyRepository) {
        this.resourceRepository = resourceRepository;
        this.historyRepository = historyRepository;
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
