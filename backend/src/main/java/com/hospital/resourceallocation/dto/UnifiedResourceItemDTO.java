package com.hospital.resourceallocation.dto;

/**
 * Unified Resource Item DTO
 * Displays hospital resources, including beds, equipment, supplies, and facilities,
 * with real-time status: Available or Occupied, and current patient allocation.
 */
public class UnifiedResourceItemDTO {

    private String id; // BED-101 or RES-EQ-01
    private String name; // Bed GEN-01 or Oxygen Cylinder
    private String category; // BED, EQUIPMENT, SUPPLY, FACILITY
    private String subType; // GENERAL, ICU, EMERGENCY, REUSABLE, CONSUMABLE
    private String status; // AVAILABLE, OCCUPIED, LOW_STOCK, MAINTENANCE
    private Integer totalUnits;
    private Integer availableUnits;
    private Integer allocatedUnits;
    private String allocatedPatientId;
    private String allocatedPatientName;
    private String location; // e.g. "Ward A - Ground Floor" or "H-001 Primary Hospital"

    public UnifiedResourceItemDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubType() { return subType; }
    public void setSubType(String subType) { this.subType = subType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalUnits() { return totalUnits; }
    public void setTotalUnits(Integer totalUnits) { this.totalUnits = totalUnits; }

    public Integer getAvailableUnits() { return availableUnits; }
    public void setAvailableUnits(Integer availableUnits) { this.availableUnits = availableUnits; }

    public Integer getAllocatedUnits() { return allocatedUnits; }
    public void setAllocatedUnits(Integer allocatedUnits) { this.allocatedUnits = allocatedUnits; }

    public String getAllocatedPatientId() { return allocatedPatientId; }
    public void setAllocatedPatientId(String allocatedPatientId) { this.allocatedPatientId = allocatedPatientId; }

    public String getAllocatedPatientName() { return allocatedPatientName; }
    public void setAllocatedPatientName(String allocatedPatientName) { this.allocatedPatientName = allocatedPatientName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
