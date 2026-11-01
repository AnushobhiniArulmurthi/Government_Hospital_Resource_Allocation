package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Admission Entity
 * Manages admitted patients, allocated beds, admitting doctors, and discharge state.
 */
@Entity
@Table(name = "admissions")
public class Admission {

    @Id
    @Column(name = "admission_id", length = 20)
    private String admissionId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "doctor_id", nullable = false, length = 20)
    private String doctorId;

    @Column(name = "hospital_id", nullable = false, length = 20)
    private String hospitalId;

    @Column(name = "bed_id", length = 20)
    private String bedId;

    @Column(name = "admission_date", nullable = false)
    private LocalDateTime admissionDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, DISCHARGED, TRANSFERRED

    @Column(name = "admission_notes", columnDefinition = "TEXT")
    private String admissionNotes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Admission() {}

    public Admission(String admissionId, String visitId, String patientId, String doctorId,
                     String hospitalId, String bedId, String admissionNotes) {
        this.admissionId = admissionId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.hospitalId = hospitalId;
        this.bedId = bedId;
        this.admissionDate = LocalDateTime.now();
        this.status = "ACTIVE";
        this.admissionNotes = admissionNotes;
    }

    // Getters and Setters
    public String getAdmissionId() { return admissionId; }
    public void setAdmissionId(String admissionId) { this.admissionId = admissionId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public LocalDateTime getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDateTime admissionDate) { this.admissionDate = admissionDate; }

    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdmissionNotes() { return admissionNotes; }
    public void setAdmissionNotes(String admissionNotes) { this.admissionNotes = admissionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
