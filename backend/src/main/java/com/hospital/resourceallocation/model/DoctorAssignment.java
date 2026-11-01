package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Doctor Assignment Entity
 * Distinguishes consultation assignments from inpatient care duties.
 */
@Entity
@Table(name = "doctor_assignments")
public class DoctorAssignment {

    @Id
    @Column(name = "assignment_id", length = 20)
    private String assignmentId;

    @Column(name = "doctor_id", nullable = false, length = 20)
    private String doctorId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "assignment_type", nullable = false, length = 30)
    private String assignmentType; // CONSULTATION, INPATIENT_CARE

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, CANCELLED

    public DoctorAssignment() {}

    public DoctorAssignment(String assignmentId, String doctorId, String patientId, String visitId, String assignmentType) {
        this.assignmentId = assignmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.visitId = visitId;
        this.assignmentType = assignmentType;
        this.startTime = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getAssignmentType() { return assignmentType; }
    public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
