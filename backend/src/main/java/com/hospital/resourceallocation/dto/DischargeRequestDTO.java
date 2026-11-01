package com.hospital.resourceallocation.dto;

import java.util.List;

/**
 * DTO for Patient Discharge and Resource Deallocation.
 */
public class DischargeRequestDTO {

    private String visitId;
    private String dischargeSummary;
    private String followUpInstructions;
    private List<String> consumablesUsed;

    public DischargeRequestDTO() {}

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getDischargeSummary() { return dischargeSummary; }
    public void setDischargeSummary(String dischargeSummary) { this.dischargeSummary = dischargeSummary; }

    public String getFollowUpInstructions() { return followUpInstructions; }
    public void setFollowUpInstructions(String followUpInstructions) { this.followUpInstructions = followUpInstructions; }

    public List<String> getConsumablesUsed() { return consumablesUsed; }
    public void setConsumablesUsed(List<String> consumablesUsed) { this.consumablesUsed = consumablesUsed; }
}
