package com.hospital.resourceallocation.dto;

import java.time.LocalDateTime;

/**
 * DTO for Active Inpatients Eligible for Clinical Discharge
 */
public class AdmittedPatientDTO {

    private String admissionId;
    private String visitId;
    private String patientId;
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String contactNumber;
    private String bedId;
    private String bedNumber;
    private String bedType;
    private String floorWard;
    private String doctorId;
    private String doctorName;
    private String medicalCondition;
    private String severity;
    private LocalDateTime admissionDate;
    private LocalDateTime currentDischargeTime;

    public AdmittedPatientDTO() {
        this.currentDischargeTime = LocalDateTime.now();
    }

    public String getAdmissionId() { return admissionId; }
    public void setAdmissionId(String admissionId) { this.admissionId = admissionId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public String getFloorWard() { return floorWard; }
    public void setFloorWard(String floorWard) { this.floorWard = floorWard; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDateTime getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDateTime admissionDate) { this.admissionDate = admissionDate; }

    public LocalDateTime getCurrentDischargeTime() { return currentDischargeTime; }
    public void setCurrentDischargeTime(LocalDateTime currentDischargeTime) { this.currentDischargeTime = currentDischargeTime; }
}
