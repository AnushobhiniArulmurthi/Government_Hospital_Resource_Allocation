package com.hospital.resourceallocation.dto;

import java.util.List;

/**
 * DTO for Initiating or Completing a Patient Referral.
 */
public class ReferralRequestDTO {

    private String visitId;
    private String fromHospitalId;
    private String targetHospitalId; // Optional: if empty, Dijkstra will auto-select the fastest eligible referral hospital
    private String reason;
    private String requiredBedType;
    private String requiredSpecialization;
    private List<String> requiredResources;
    private List<String> requiredDiagnostics;

    public ReferralRequestDTO() {}

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getFromHospitalId() { return fromHospitalId; }
    public void setFromHospitalId(String fromHospitalId) { this.fromHospitalId = fromHospitalId; }

    public String getTargetHospitalId() { return targetHospitalId; }
    public void setTargetHospitalId(String targetHospitalId) { this.targetHospitalId = targetHospitalId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRequiredBedType() { return requiredBedType; }
    public void setRequiredBedType(String requiredBedType) { this.requiredBedType = requiredBedType; }

    public String getRequiredSpecialization() { return requiredSpecialization; }
    public void setRequiredSpecialization(String requiredSpecialization) { this.requiredSpecialization = requiredSpecialization; }

    public List<String> getRequiredResources() { return requiredResources; }
    public void setRequiredResources(List<String> requiredResources) { this.requiredResources = requiredResources; }

    public List<String> getRequiredDiagnostics() { return requiredDiagnostics; }
    public void setRequiredDiagnostics(List<String> requiredDiagnostics) { this.requiredDiagnostics = requiredDiagnostics; }
}
