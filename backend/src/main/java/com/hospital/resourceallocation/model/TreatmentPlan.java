package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Treatment Plan Entity
 * Doctor-prescribed clinical pathway specifying required bed types, specializations,
 * diagnostic orders, and flexible resource selections.
 */
@Entity
@Table(name = "treatment_plans")
public class TreatmentPlan {

    @Id
    @Column(name = "treatment_plan_id", length = 20)
    private String treatmentPlanId;

    @Column(name = "consultation_id", nullable = false, length = 20)
    private String consultationId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "doctor_id", nullable = false, length = 20)
    private String doctorId;

    @Column(name = "treatment_category", nullable = false, length = 40)
    private String treatmentCategory; // CONSULTATION_ONLY, MEDICATION_BASIC, DIAGNOSTIC_TESTS_REQUIRED, HOSPITAL_ADMISSION_REQUIRED, EMERGENCY_TREATMENT, REFERRAL_REQUIRED, FOLLOW_UP

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "required_bed_type", length = 30)
    private String requiredBedType; // GENERAL, ICU, EMERGENCY, NO_BED

    @Column(name = "required_doctor_specialization", length = 80)
    private String requiredDoctorSpecialization;

    @Column(name = "required_resources", columnDefinition = "TEXT")
    private String requiredResources; // Comma-separated list or JSON of resources

    @Column(name = "diagnostic_orders_summary", columnDefinition = "TEXT")
    private String diagnosticOrdersSummary;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "CREATED"; // CREATED, IN_PROGRESS, COMPLETED, CANCELLED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public TreatmentPlan() {}

    public TreatmentPlan(String treatmentPlanId, String consultationId, String visitId, String patientId,
                         String doctorId, String treatmentCategory, String doctorNotes,
                         String requiredBedType, String requiredDoctorSpecialization,
                         String requiredResources, String diagnosticOrdersSummary) {
        this.treatmentPlanId = treatmentPlanId;
        this.consultationId = consultationId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.treatmentCategory = treatmentCategory;
        this.doctorNotes = doctorNotes;
        this.requiredBedType = requiredBedType != null ? requiredBedType : "NO_BED";
        this.requiredDoctorSpecialization = requiredDoctorSpecialization;
        this.requiredResources = requiredResources;
        this.diagnosticOrdersSummary = diagnosticOrdersSummary;
        this.status = "CREATED";
    }

    public boolean requiresAdmission() {
        return "HOSPITAL_ADMISSION_REQUIRED".equalsIgnoreCase(treatmentCategory)
                || "EMERGENCY_TREATMENT".equalsIgnoreCase(treatmentCategory)
                || (requiredBedType != null && !"NO_BED".equalsIgnoreCase(requiredBedType) && !requiredBedType.trim().isEmpty());
    }

    // Getters and Setters
    public String getTreatmentPlanId() { return treatmentPlanId; }
    public void setTreatmentPlanId(String treatmentPlanId) { this.treatmentPlanId = treatmentPlanId; }

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getTreatmentCategory() { return treatmentCategory; }
    public void setTreatmentCategory(String treatmentCategory) { this.treatmentCategory = treatmentCategory; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    public String getRequiredBedType() { return requiredBedType; }
    public void setRequiredBedType(String requiredBedType) { this.requiredBedType = requiredBedType; }

    public String getRequiredDoctorSpecialization() { return requiredDoctorSpecialization; }
    public void setRequiredDoctorSpecialization(String requiredDoctorSpecialization) { this.requiredDoctorSpecialization = requiredDoctorSpecialization; }

    public String getRequiredResources() { return requiredResources; }
    public void setRequiredResources(String requiredResources) { this.requiredResources = requiredResources; }

    public String getDiagnosticOrdersSummary() { return diagnosticOrdersSummary; }
    public void setDiagnosticOrdersSummary(String diagnosticOrdersSummary) { this.diagnosticOrdersSummary = diagnosticOrdersSummary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
