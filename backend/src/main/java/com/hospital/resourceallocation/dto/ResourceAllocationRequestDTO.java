package com.hospital.resourceallocation.dto;

import java.util.List;

/**
 * DTO for Requesting Allocation of Resources for a Patient Visit.
 */
public class ResourceAllocationRequestDTO {

    private String visitId;
    private String bedId;
    private String doctorId;
    private List<String> resourceNames;
    private Integer quantity;

    public ResourceAllocationRequestDTO() {}

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public List<String> getResourceNames() { return resourceNames; }
    public void setResourceNames(List<String> resourceNames) { this.resourceNames = resourceNames; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
