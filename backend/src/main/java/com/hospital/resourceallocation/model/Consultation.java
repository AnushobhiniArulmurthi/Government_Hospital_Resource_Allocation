package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Consultation Entity
 * Manages doctor-patient examination, recording symptoms, observations, and prescriptions.
 */
@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @Column(name = "consultation_id", length = 20)
    private String consultationId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "doctor_id", nullable = false, length = 20)
    private String doctorId;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "doctor_observations", columnDefinition = "TEXT")
    private String doctorObservations;

    @Column(name = "prescription", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Consultation() {}

    public Consultation(String consultationId, String visitId, String patientId, String doctorId) {
        this.consultationId = consultationId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.status = "ASSIGNED";
        this.startedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDoctorObservations() { return doctorObservations; }
    public void setDoctorObservations(String doctorObservations) { this.doctorObservations = doctorObservations; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
