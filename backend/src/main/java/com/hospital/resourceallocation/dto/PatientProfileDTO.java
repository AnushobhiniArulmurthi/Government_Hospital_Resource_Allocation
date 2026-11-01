package com.hospital.resourceallocation.dto;

import com.hospital.resourceallocation.model.*;
import java.util.List;

/**
 * Unified Patient 360 Profile DTO
 * Aggregates complete clinical, operational, and historical records for a patient:
 * - Demographics & preferred doctor
 * - Visit history & triage severities
 * - Doctor consultation notes
 * - Treatment plans
 * - Diagnostic orders, lab reports & doctor review remarks
 * - Allocated beds & medical resources
 * - Inpatient admissions & inter-hospital referrals
 * - Discharge summaries & follow-up plans
 */
public class PatientProfileDTO {

    private Patient patient;
    private Doctor preferredDoctor;
    private List<PatientVisit> visits;
    private List<Consultation> consultations;
    private List<TreatmentPlan> treatmentPlans;
    private List<DiagnosticOrder> diagnosticOrders;
    private List<Admission> admissions;
    private List<DischargeHistory> dischargeHistories;
    private List<Referral> referrals;
    private List<ResourceAllocation> resourceAllocations;

    public PatientProfileDTO() {}

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getPreferredDoctor() { return preferredDoctor; }
    public void setPreferredDoctor(Doctor preferredDoctor) { this.preferredDoctor = preferredDoctor; }

    public List<PatientVisit> getVisits() { return visits; }
    public void setVisits(List<PatientVisit> visits) { this.visits = visits; }

    public List<Consultation> getConsultations() { return consultations; }
    public void setConsultations(List<Consultation> consultations) { this.consultations = consultations; }

    public List<TreatmentPlan> getTreatmentPlans() { return treatmentPlans; }
    public void setTreatmentPlans(List<TreatmentPlan> treatmentPlans) { this.treatmentPlans = treatmentPlans; }

    public List<DiagnosticOrder> getDiagnosticOrders() { return diagnosticOrders; }
    public void setDiagnosticOrders(List<DiagnosticOrder> diagnosticOrders) { this.diagnosticOrders = diagnosticOrders; }

    public List<Admission> getAdmissions() { return admissions; }
    public void setAdmissions(List<Admission> admissions) { this.admissions = admissions; }

    public List<DischargeHistory> getDischargeHistories() { return dischargeHistories; }
    public void setDischargeHistories(List<DischargeHistory> dischargeHistories) { this.dischargeHistories = dischargeHistories; }
    public List<DischargeHistory> getDischarges() { return dischargeHistories; }
    public void setDischarges(List<DischargeHistory> discharges) { this.dischargeHistories = discharges; }

    public List<Referral> getReferrals() { return referrals; }
    public void setReferrals(List<Referral> referrals) { this.referrals = referrals; }

    public List<ResourceAllocation> getResourceAllocations() { return resourceAllocations; }
    public void setResourceAllocations(List<ResourceAllocation> resourceAllocations) { this.resourceAllocations = resourceAllocations; }
}
