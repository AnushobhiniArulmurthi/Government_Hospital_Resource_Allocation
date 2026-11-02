package com.hospital.resourceallocation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Doctor Resource Prescriptions
 * Contains prescribed bed requirement, diagnostic scans (MRI, CT, etc.),
 * and treatments/equipment (Ventilator, Oxygen, etc.).
 */
public class PrescriptionRequestDTO {

    @NotBlank(message = "Patient ID is mandatory")
    private String patientId;

    private String doctorId; // Prescribing Doctor ID (defaults to assigned or D-001)

    private Boolean admissionRequired = false;

    private String bedType; // GENERAL, ICU, EMERGENCY, NONE

    private List<String> services = new ArrayList<>(); // MRI_SCAN, CT_SCAN, X_RAY, ULTRASOUND, BLOOD_TEST

    private List<String> treatments = new ArrayList<>(); // Oxygen Cylinder, Ventilator, Infusion Pump, etc.

    private String notes; // Doctor prescription instructions and medication

    private String severity; // Optional updated severity: Critical, Serious, Normal

    public PrescriptionRequestDTO() {}

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public Boolean getAdmissionRequired() { return admissionRequired; }
    public void setAdmissionRequired(Boolean admissionRequired) { this.admissionRequired = admissionRequired; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services != null ? services : new ArrayList<>(); }

    public List<String> getTreatments() { return treatments; }
    public void setTreatments(List<String> treatments) { this.treatments = treatments != null ? treatments : new ArrayList<>(); }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
