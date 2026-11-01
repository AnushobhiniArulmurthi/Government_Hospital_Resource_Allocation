package com.hospital.resourceallocation.dto;

import java.util.List;

public class TreatmentPlanRequestDTO {

    private String consultationId;
    private String visitId;
    private String doctorId;
    private String treatmentCategory; // CONSULTATION_ONLY, MEDICATION_BASIC, DIAGNOSTIC_TESTS_REQUIRED, HOSPITAL_ADMISSION_REQUIRED, EMERGENCY_TREATMENT, REFERRAL_REQUIRED, FOLLOW_UP
    private String doctorNotes;
    private String requiredBedType; // GENERAL, ICU, EMERGENCY, NO_BED
    private String requiredDoctorSpecialization;
    private List<String> requiredResources;
    private List<String> diagnosticTests; // e.g. ["X_RAY", "CT_SCAN", "BLOOD_TEST"]
    private String diagnosticCentreId; // Optional external centre

    public TreatmentPlanRequestDTO() {}

    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

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

    public List<String> getRequiredResources() { return requiredResources; }
    public void setRequiredResources(List<String> requiredResources) { this.requiredResources = requiredResources; }

    public List<String> getDiagnosticTests() { return diagnosticTests; }
    public void setDiagnosticTests(List<String> diagnosticTests) { this.diagnosticTests = diagnosticTests; }

    public String getDiagnosticCentreId() { return diagnosticCentreId; }
    public void setDiagnosticCentreId(String diagnosticCentreId) { this.diagnosticCentreId = diagnosticCentreId; }
}
