package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Resource Allocation Entity
 * Tracks active resource locks per patient visit.
 */
@Entity
@Table(name = "resource_allocations")
public class ResourceAllocation {

    @Id
    @Column(name = "allocation_id", length = 20)
    private String allocationId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "resource_id", nullable = false, length = 20)
    private String resourceId;

    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    @Column(name = "resource_type", nullable = false, length = 30)
    private String resourceType; // EQUIPMENT, SUPPLY, FACILITY

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "allocated_at", nullable = false)
    private LocalDateTime allocatedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, RELEASED

    public ResourceAllocation() {}

    public ResourceAllocation(String allocationId, String visitId, String patientId, String resourceId,
                              String resourceName, String resourceType, Integer quantity) {
        this.allocationId = allocationId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.quantity = quantity != null ? quantity : 1;
        this.allocatedAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public String getAllocationId() { return allocationId; }
    public void setAllocationId(String allocationId) { this.allocationId = allocationId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(LocalDateTime allocatedAt) { this.allocatedAt = allocatedAt; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) { this.releasedAt = releasedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
