package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Waiting Queue Item Model for Resource Allocation Waiting Queue.
 * Implements Comparable for Java PriorityQueue:
 * Primary Criterion: Severity Level (Critical=1, Serious=2, Minor=3)
 * Secondary Criterion: Arrival Time (FIFO for equal priority)
 * Deterministic Tie-breaking: Queue ID
 */
@Entity
@Table(name = "resource_waiting_queue")
public class ResourceWaitingQueueItem implements Comparable<ResourceWaitingQueueItem> {

    @Id
    @Column(name = "queue_id", length = 20)
    private String queueId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "severity_level", nullable = false, length = 20)
    private String severityLevel;

    @Column(name = "required_bed_type", length = 30)
    private String requiredBedType;

    @Column(name = "required_specialization", length = 80)
    private String requiredSpecialization;

    @Column(name = "required_resources", columnDefinition = "TEXT")
    private String requiredResources;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "priority_score", nullable = false)
    private Integer priorityScore;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "WAITING"; // WAITING, ALLOCATED, CANCELLED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ResourceWaitingQueueItem() {}

    public ResourceWaitingQueueItem(String queueId, String visitId, String patientId,
                                    String severityLevel, String requiredBedType,
                                    String requiredSpecialization, String requiredResources,
                                    LocalDateTime arrivalTime) {
        this.queueId = queueId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.severityLevel = severityLevel;
        this.requiredBedType = requiredBedType;
        this.requiredSpecialization = requiredSpecialization;
        this.requiredResources = requiredResources;
        this.arrivalTime = arrivalTime != null ? arrivalTime : LocalDateTime.now();
        this.priorityScore = calculateScore(severityLevel);
        this.status = "WAITING";
    }

    private int calculateScore(String severity) {
        if ("Critical".equalsIgnoreCase(severity)) return 1;
        if ("Serious".equalsIgnoreCase(severity)) return 2;
        return 3; // Minor
    }

    @Override
    public int compareTo(ResourceWaitingQueueItem other) {
        if (other == null) return -1;
        int scoreCompare = Integer.compare(this.priorityScore, other.priorityScore);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        int timeCompare = this.arrivalTime.compareTo(other.arrivalTime);
        if (timeCompare != 0) {
            return timeCompare;
        }
        return this.queueId.compareTo(other.queueId);
    }

    // Getters and Setters
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
        this.priorityScore = calculateScore(severityLevel);
    }

    public String getRequiredBedType() { return requiredBedType; }
    public void setRequiredBedType(String requiredBedType) { this.requiredBedType = requiredBedType; }

    public String getRequiredSpecialization() { return requiredSpecialization; }
    public void setRequiredSpecialization(String requiredSpecialization) { this.requiredSpecialization = requiredSpecialization; }

    public String getRequiredResources() { return requiredResources; }
    public void setRequiredResources(String requiredResources) { this.requiredResources = requiredResources; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public Integer getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
