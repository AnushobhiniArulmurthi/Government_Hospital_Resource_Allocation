package com.hospital.resourceallocation.dto;

import java.time.LocalDateTime;

/**
 * Detailed DTO for Inter-Hospital Referrals
 * Aggregates patient details, clinical severity, required resources,
 * destination hospital contact info, Dijkstra route, and approval state.
 */
public class ReferralDetailDTO {

    private String referralId;
    private String visitId;
    private String patientId;
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String patientContact;
    private String severity; // Critical, Serious, Normal
    private String medicalCondition;
    private String reason;
    private String fromHospitalId;
    private String toHospitalId;
    private String toHospitalName;
    private String toHospitalContact;
    private String toHospitalAddress;
    private Integer travelTimeMinutes;
    private String routePath;
    private String status; // PENDING_APPROVAL, CONFIRMED, COMPLETED, CANCELLED
    private LocalDateTime createdAt;

    public ReferralDetailDTO() {}

    public String getReferralId() { return referralId; }
    public void setReferralId(String referralId) { this.referralId = referralId; }

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

    public String getPatientContact() { return patientContact; }
    public void setPatientContact(String patientContact) { this.patientContact = patientContact; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getFromHospitalId() { return fromHospitalId; }
    public void setFromHospitalId(String fromHospitalId) { this.fromHospitalId = fromHospitalId; }

    public String getToHospitalId() { return toHospitalId; }
    public void setToHospitalId(String toHospitalId) { this.toHospitalId = toHospitalId; }

    public String getToHospitalName() { return toHospitalName; }
    public void setToHospitalName(String toHospitalName) { this.toHospitalName = toHospitalName; }

    public String getToHospitalContact() { return toHospitalContact; }
    public void setToHospitalContact(String toHospitalContact) { this.toHospitalContact = toHospitalContact; }

    public String getToHospitalAddress() { return toHospitalAddress; }
    public void setToHospitalAddress(String toHospitalAddress) { this.toHospitalAddress = toHospitalAddress; }

    public Integer getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(Integer travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }

    public String getRoutePath() { return routePath; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
