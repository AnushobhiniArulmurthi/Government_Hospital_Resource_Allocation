package com.hospital.resourceallocation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for clinical triage severity assignment by authorized medical/doctor staff.
 */
public class TriageAssessmentDTO {

    @NotBlank(message = "Triage severity level is mandatory")
    @Pattern(regexp = "^(?i)(Critical|Serious|Minor)$", message = "Severity level must be Critical, Serious, or Minor")
    private String severityLevel;

    private String triageNotes;
    private String assessedBy;

    public TriageAssessmentDTO() {}

    public TriageAssessmentDTO(String severityLevel, String triageNotes, String assessedBy) {
        this.severityLevel = severityLevel;
        this.triageNotes = triageNotes;
        this.assessedBy = assessedBy;
    }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    public String getTriageNotes() { return triageNotes; }
    public void setTriageNotes(String triageNotes) { this.triageNotes = triageNotes; }

    public String getAssessedBy() { return assessedBy; }
    public void setAssessedBy(String assessedBy) { this.assessedBy = assessedBy; }
}
