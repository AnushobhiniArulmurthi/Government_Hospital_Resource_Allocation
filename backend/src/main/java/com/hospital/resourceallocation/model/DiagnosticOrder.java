package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Diagnostic Order Entity
 * Tracks doctor-prescribed scans/tests, scheduling, external centre routing,
 * lab observations, and doctor reviews.
 * Statuses: PRESCRIBED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED.
 */
@Entity
@Table(name = "diagnostic_orders")
public class DiagnosticOrder {

    @Id
    @Column(name = "order_id", length = 20)
    private String orderId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "doctor_id", nullable = false, length = 20)
    private String doctorId;

    @Column(name = "test_type", nullable = false, length = 50)
    private String testType; // X_RAY, CT_SCAN, MRI_SCAN, ULTRASOUND, BLOOD_TEST, LAB_TEST

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "MINOR"; // CRITICAL, SERIOUS, MINOR

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PRESCRIBED"; // PRESCRIBED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "diagnostic_centre_id", length = 20)
    private String diagnosticCentreId;

    @Column(name = "prescribed_date", nullable = false)
    private LocalDateTime prescribedDate;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "results_summary", columnDefinition = "TEXT")
    private String resultsSummary;

    @Column(name = "doctor_review_notes", columnDefinition = "TEXT")
    private String doctorReviewNotes;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by_doctor_id", length = 20)
    private String reviewedByDoctorId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public DiagnosticOrder() {}

    public DiagnosticOrder(String orderId, String visitId, String patientId, String doctorId,
                           String testType, String priority, String diagnosticCentreId) {
        this.orderId = orderId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testType = testType;
        this.priority = priority != null ? priority : "MINOR";
        this.diagnosticCentreId = diagnosticCentreId;
        this.prescribedDate = LocalDateTime.now();
        this.status = "PRESCRIBED";
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDiagnosticCentreId() { return diagnosticCentreId; }
    public void setDiagnosticCentreId(String diagnosticCentreId) { this.diagnosticCentreId = diagnosticCentreId; }

    public LocalDateTime getPrescribedDate() { return prescribedDate; }
    public void setPrescribedDate(LocalDateTime prescribedDate) { this.prescribedDate = prescribedDate; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getResultsSummary() { return resultsSummary; }
    public void setResultsSummary(String resultsSummary) { this.resultsSummary = resultsSummary; }

    public String getDoctorReviewNotes() { return doctorReviewNotes; }
    public void setDoctorReviewNotes(String doctorReviewNotes) { this.doctorReviewNotes = doctorReviewNotes; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewedByDoctorId() { return reviewedByDoctorId; }
    public void setReviewedByDoctorId(String reviewedByDoctorId) { this.reviewedByDoctorId = reviewedByDoctorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
