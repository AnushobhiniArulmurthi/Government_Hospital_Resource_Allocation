package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.dto.ResourceAllocationRequestDTO;
import com.hospital.resourceallocation.model.Resource;
import com.hospital.resourceallocation.model.ResourceAllocation;
import com.hospital.resourceallocation.model.ResourceHistory;
import com.hospital.resourceallocation.service.ResourceAllocationEngineService;
import com.hospital.resourceallocation.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Resource Management and Centralized Allocation Engine REST Controller
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceAllocationEngineService allocationEngine;

    @Autowired
    public ResourceController(ResourceService resourceService,
                              ResourceAllocationEngineService allocationEngine) {
        this.resourceService = resourceService;
        this.allocationEngine = allocationEngine;
    }

    @GetMapping
    public ApiResponse<List<Resource>> getAllResources(@RequestParam(defaultValue = "H-001") String hospitalId) {
        return ApiResponse.ok(resourceService.getResourcesByHospital(hospitalId));
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<Resource>> getByCategory(@PathVariable String category,
                                                     @RequestParam(defaultValue = "H-001") String hospitalId) {
        return ApiResponse.ok(resourceService.getResourcesByCategory(hospitalId, category));
    }

    @PostMapping("/allocate")
    public ApiResponse<List<ResourceAllocation>> allocateResources(@RequestBody ResourceAllocationRequestDTO dto) {
        List<ResourceAllocation> allocations = allocationEngine.allocateResources(dto);
        return ApiResponse.ok("Resources successfully reserved for visit: " + dto.getVisitId(), allocations);
    }

    @PostMapping("/release")
    public ApiResponse<Resource> releaseResource(@RequestBody Map<String, Object> body) {
        String resourceId = (String) body.get("resourceId");
        int quantity = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        String patientId = (String) body.get("patientId");
        String visitId = (String) body.get("visitId");

        Resource released = resourceService.releaseResource(resourceId, quantity, patientId, visitId);
        return ApiResponse.ok("Resource released successfully", released);
    }

    @GetMapping("/history")
    public ApiResponse<List<ResourceHistory>> getResourceHistory(@RequestParam(required = false) String visitId) {
        if (visitId != null && !visitId.trim().isEmpty()) {
            return ApiResponse.ok(resourceService.getHistoryByVisitId(visitId));
        }
        return ApiResponse.ok(resourceService.getAllResourceHistory());
    }

    @GetMapping("/allocations")
    public ApiResponse<List<ResourceAllocation>> getAllAllocations() {
        return ApiResponse.ok(allocationEngine.getAllAllocations());
    }
}
