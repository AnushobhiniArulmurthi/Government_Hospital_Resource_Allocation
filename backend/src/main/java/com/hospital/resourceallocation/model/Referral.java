package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Referral Entity
 * Records patient transfers to referral hospitals selected by Dijkstra's shortest path algorithm.
 */
@Entity
@Table(name = "referrals")
public class Referral {

    @Id
    @Column(name = "referral_id", length = 20)
    private String referralId;

    @Column(name = "visit_id", nullable = false, length = 20)
    private String visitId;

    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @Column(name = "from_hospital_id", nullable = false, length = 20)
    private String fromHospitalId;

    @Column(name = "to_hospital_id", nullable = false, length = 20)
    private String toHospitalId;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "travel_time_minutes", nullable = false)
    private Integer travelTimeMinutes = 0;

    @Column(name = "route_path", nullable = false, length = 255)
    private String routePath;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "CONFIRMED"; // PENDING, CONFIRMED, COMPLETED, CANCELLED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Referral() {}

    public Referral(String referralId, String visitId, String patientId, String fromHospitalId,
                    String toHospitalId, String reason, Integer travelTimeMinutes, String routePath) {
        this.referralId = referralId;
        this.visitId = visitId;
        this.patientId = patientId;
        this.fromHospitalId = fromHospitalId;
        this.toHospitalId = toHospitalId;
        this.reason = reason;
        this.travelTimeMinutes = travelTimeMinutes != null ? travelTimeMinutes : 0;
        this.routePath = routePath;
        this.status = "CONFIRMED";
    }

    // Getters and Setters
    public String getReferralId() { return referralId; }
    public void setReferralId(String referralId) { this.referralId = referralId; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFromHospitalId() { return fromHospitalId; }
    public void setFromHospitalId(String fromHospitalId) { this.fromHospitalId = fromHospitalId; }

    public String getToHospitalId() { return toHospitalId; }
    public void setToHospitalId(String toHospitalId) { this.toHospitalId = toHospitalId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getTravelTimeMinutes() { return travelTimeMinutes; }
    public void setTravelTimeMinutes(Integer travelTimeMinutes) { this.travelTimeMinutes = travelTimeMinutes; }

    public String getRoutePath() { return routePath; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
