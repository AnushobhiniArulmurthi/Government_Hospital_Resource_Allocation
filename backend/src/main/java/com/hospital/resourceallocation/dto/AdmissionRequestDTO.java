package com.hospital.resourceallocation.dto;

import java.util.List;

public class AdmissionRequestDTO {

    private String visitId;
    private String doctorId;
    private String bedId; // Optional specific bed, or auto-selected by bed type
    private String bedType; // GENERAL, ICU, EMERGENCY
    private String hospitalId;
    private List<String> requiredResources;
    private String notes;

    public AdmissionRequestDTO() {}

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public List<String> getRequiredResources() { return requiredResources; }
    public void setRequiredResources(List<String> requiredResources) { this.requiredResources = requiredResources; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
