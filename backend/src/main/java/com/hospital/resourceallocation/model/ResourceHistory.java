package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Resource History Entity
 * Immutable audit log of all resource events: ALLOCATED, RELEASED, CONSUMED, RESTOCKED.
 */
@Entity
@Table(name = "resource_history")
public class ResourceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer historyId;

    @Column(name = "resource_id", nullable = false, length = 20)
    private String resourceId;

    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType; // ALLOCATED, RELEASED, CONSUMED, RESTOCKED

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "patient_id", length = 20)
    private String patientId;

    @Column(name = "visit_id", length = 20)
    private String visitId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "remarks", length = 255)
    private String remarks;

    public ResourceHistory() {}

    public ResourceHistory(String resourceId, String resourceName, String actionType, Integer quantity,
                           String patientId, String visitId, String remarks) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.actionType = actionType;
        this.quantity = quantity;
        this.patientId = patientId;
        this.visitId = visitId;
        this.timestamp = LocalDateTime.now();
        this.remarks = remarks;
    }

    // Getters and Setters
    public Integer getHistoryId() { return historyId; }
    public void setHistoryId(Integer historyId) { this.historyId = historyId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
