package com.hospital.resourceallocation.dto;

import java.time.LocalDateTime;

/**
 * Detailed DTO for Severity-Based Priority Queue Items
 * Includes citizen demographics, medical condition, priority ranking,
 * and requested resources.
 */
public class QueueItemDetailDTO {

    private String queueId;
    private String visitId;
    private String patientId;
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private String severityLevel; // Critical, Serious, Normal
    private Integer priorityScore; // 1=Critical, 2=Serious, 3=Normal
    private String requiredBedType;
    private String requiredResources;
    private String medicalCondition;
    private String status; // WAITING, ALLOCATED, CANCELLED
    private LocalDateTime arrivalTime;

    public QueueItemDetailDTO() {}

    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    public Integer getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }

    public String getRequiredBedType() { return requiredBedType; }
    public void setRequiredBedType(String requiredBedType) { this.requiredBedType = requiredBedType; }

    public String getRequiredResources() { return requiredResources; }
    public void setRequiredResources(String requiredResources) { this.requiredResources = requiredResources; }

    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
}
