package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Patient Visit Entity
 * Represents an individual clinical visit episode for a patient.
 * Tracks clinical triage, preferred vs assigned doctor workflow,
 * admission status, and complete episode history.
 */
@Entity
@Table(name = "patient_visits")
public class PatientVisit {

    @Id
    @Column(name = "visit_id", length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "severity_level", nullable = false, length = 30)
    private String severityLevel = "PENDING_TRIAGE"; // PENDING_TRIAGE, Critical, Serious, Minor

    @Column(name = "patient_category", nullable = false, length = 30)
    private String patientCategory = "Outpatient"; // Outpatient, Diagnostic-Based, Inpatient, Emergency

    @Column(name = "visit_status", nullable = false, length = 60)
    private String visitStatus = "PENDING_TRIAGE"; // PENDING_TRIAGE, TRIAGED, IN_CONSULTATION, TREATMENT_PLANNED, ADMITTED, REFERRED, NO_DOCTOR_AVAILABLE, DISCHARGED, COMPLETED

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "assigned_doctor_id", length = 20)
    private String assignedDoctorId;

    @Column(name = "preferred_doctor_id", length = 20)
    private String preferredDoctorId;

    @Column(name = "alternative_doctor_offered")
    private Boolean alternativeDoctorOffered = false;

    @Column(name = "alternative_doctor_accepted")
    private Boolean alternativeDoctorAccepted;

    @Column(name = "assigned_bed_id", length = 20)
    private String assignedBedId;

    @Column(name = "discharge_time")
    private LocalDateTime dischargeTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "triage_notes", columnDefinition = "TEXT")
    private String triageNotes;

    @Column(name = "triage_assessed_by", length = 100)
    private String triageAssessedBy;

    @Column(name = "triage_assessed_time")
    private LocalDateTime triageAssessedTime;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public PatientVisit() {}

    public PatientVisit(String visitId, String patientId, String severityLevel, String patientCategory,
                        LocalDateTime arrivalTime, String notes) {
        this.visitId = visitId;
        this.patientId = patientId;
        this.severityLevel = severityLevel != null ? severityLevel : "PENDING_TRIAGE";
        this.patientCategory = patientCategory != null ? patientCategory : "Outpatient";
        this.visitStatus = "PENDING_TRIAGE";
        this.arrivalTime = arrivalTime != null ? arrivalTime : LocalDateTime.now();
        this.notes = notes;
    }

    public int getPriorityScore() {
        if ("Critical".equalsIgnoreCase(this.severityLevel)) return 1;
        if ("Serious".equalsIgnoreCase(this.severityLevel)) return 2;
        if ("Minor".equalsIgnoreCase(this.severityLevel)) return 3;
        return 4; // Pending triage
    }

    // Getters and Setters
    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    public String getPatientCategory() { return patientCategory; }
    public void setPatientCategory(String patientCategory) { this.patientCategory = patientCategory; }

    public String getVisitStatus() { return visitStatus; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getAssignedDoctorId() { return assignedDoctorId; }
    public void setAssignedDoctorId(String assignedDoctorId) { this.assignedDoctorId = assignedDoctorId; }

    public String getPreferredDoctorId() { return preferredDoctorId; }
    public void setPreferredDoctorId(String preferredDoctorId) { this.preferredDoctorId = preferredDoctorId; }

    public Boolean getAlternativeDoctorOffered() { return alternativeDoctorOffered; }
    public void setAlternativeDoctorOffered(Boolean alternativeDoctorOffered) { this.alternativeDoctorOffered = alternativeDoctorOffered; }

    public Boolean getAlternativeDoctorAccepted() { return alternativeDoctorAccepted; }
    public void setAlternativeDoctorAccepted(Boolean alternativeDoctorAccepted) { this.alternativeDoctorAccepted = alternativeDoctorAccepted; }

    public String getAssignedBedId() { return assignedBedId; }
    public void setAssignedBedId(String assignedBedId) { this.assignedBedId = assignedBedId; }

    public LocalDateTime getDischargeTime() { return dischargeTime; }
    public void setDischargeTime(LocalDateTime dischargeTime) { this.dischargeTime = dischargeTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTriageNotes() { return triageNotes; }
    public void setTriageNotes(String triageNotes) { this.triageNotes = triageNotes; }

    public String getTriageAssessedBy() { return triageAssessedBy; }
    public void setTriageAssessedBy(String triageAssessedBy) { this.triageAssessedBy = triageAssessedBy; }

    public LocalDateTime getTriageAssessedTime() { return triageAssessedTime; }
    public void setTriageAssessedTime(LocalDateTime triageAssessedTime) { this.triageAssessedTime = triageAssessedTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
