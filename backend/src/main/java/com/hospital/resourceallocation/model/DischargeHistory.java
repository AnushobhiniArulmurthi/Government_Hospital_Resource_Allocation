package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Discharge History Entity
 * Formal clinical discharge record with summary of treatment and deallocated assets.
 */
@Entity
@Table(name = "discharge_history")
public class DischargeHistory {

    @Id
    @Column(name = "discharge_id", length = 20)
    private String dischargeId;

    @Column(name = "admission_id", nullable = false, length = 20)
    private String admissionId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "discharge_date", nullable = false)
    private LocalDateTime dischargeDate;

    @Column(name = "discharge_summary", columnDefinition = "TEXT", nullable = false)
    private String dischargeSummary;

    @Column(name = "resources_released", columnDefinition = "TEXT")
    private String resourcesReleased;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public DischargeHistory() {}

    public DischargeHistory(String dischargeId, String admissionId, String visitId, String patientId,
                            String dischargeSummary, String resourcesReleased, String followUpInstructions) {
        this.dischargeId = dischargeId;
        this.admissionId = admissionId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.dischargeDate = LocalDateTime.now();
        this.dischargeSummary = dischargeSummary;
        this.resourcesReleased = resourcesReleased;
        this.followUpInstructions = followUpInstructions;
    }

    // Getters and Setters
    public String getDischargeId() { return dischargeId; }
    public void setDischargeId(String dischargeId) { this.dischargeId = dischargeId; }

    public String getAdmissionId() { return admissionId; }
    public void setAdmissionId(String admissionId) { this.admissionId = admissionId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }

    public String getDischargeSummary() { return dischargeSummary; }
    public void setDischargeSummary(String dischargeSummary) { this.dischargeSummary = dischargeSummary; }

    public String getResourcesReleased() { return resourcesReleased; }
    public void setResourcesReleased(String resourcesReleased) { this.resourcesReleased = resourcesReleased; }

    public String getFollowUpInstructions() { return followUpInstructions; }
    public void setFollowUpInstructions(String followUpInstructions) { this.followUpInstructions = followUpInstructions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
