package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Base Abstract/Concrete Resource Entity.
 * Represents all hospital inventory assets: Equipment, Supplies, and Facilities.
 */
@Entity
@Table(name = "resources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "category", discriminatorType = DiscriminatorType.STRING)
public class Resource implements Allocatable {

    @Id
    @Column(name = "resource_id", length = 20)
    private String resourceId;

    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    @Column(name = "category", insertable = false, updatable = false)
    private String category; // EQUIPMENT, SUPPLY, FACILITY

    @Column(name = "hospital_id", nullable = false, length = 20)
    private String hospitalId;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity = 0;

    @Column(name = "is_reusable", nullable = false)
    private Boolean isReusable = true;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "AVAILABLE"; // AVAILABLE, LOW_STOCK, DEPLETED, MAINTENANCE

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Resource() {}

    public Resource(String resourceId, String resourceName, String category, String hospitalId,
                    Integer totalQuantity, Integer availableQuantity, Boolean isReusable) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.category = category;
        this.hospitalId = hospitalId;
        this.totalQuantity = totalQuantity != null ? totalQuantity : 0;
        this.availableQuantity = availableQuantity != null ? availableQuantity : 0;
        this.allocatedQuantity = 0;
        this.isReusable = isReusable != null ? isReusable : true;
        this.status = "AVAILABLE";
    }

    public boolean supportsRequirement(String requestedName) {
        if (requestedName == null || this.resourceName == null) return false;
        return this.resourceName.equalsIgnoreCase(requestedName.trim());
    }

    public boolean checkAvailability(int requestedUnits) {
        return this.availableQuantity != null && this.availableQuantity >= requestedUnits;
    }

    @Override
    public boolean isAvailable() {
        return checkAvailability(1);
    }

    @Override
    public void allocate(String patientId, String visitId) {
        allocateUnits(1);
    }

    public void allocateUnits(int units) {
        if (this.availableQuantity >= units) {
            this.availableQuantity -= units;
            this.allocatedQuantity += units;
            if (this.availableQuantity == 0) {
                this.status = "DEPLETED";
            }
        } else {
            throw new IllegalStateException("Insufficient inventory for resource: " + resourceName);
        }
    }

    @Override
    public void release() {
        releaseUnits(1);
    }

    public void releaseUnits(int units) {
        if (Boolean.TRUE.equals(this.isReusable)) {
            this.availableQuantity += units;
            this.allocatedQuantity = Math.max(0, this.allocatedQuantity - units);
            this.status = "AVAILABLE";
        } else {
            // For consumables, releasing simply records that allocated units were used up
            this.allocatedQuantity = Math.max(0, this.allocatedQuantity - units);
        }
    }

    @Override
    public String getResourceIdentifier() {
        return this.resourceId;
    }

    @Override
    public String getResourceCategory() {
        return this.category;
    }

    // Getters and Setters
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(Integer allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }

    public Boolean getIsReusable() { return isReusable; }
    public void setIsReusable(Boolean isReusable) { this.isReusable = isReusable; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
