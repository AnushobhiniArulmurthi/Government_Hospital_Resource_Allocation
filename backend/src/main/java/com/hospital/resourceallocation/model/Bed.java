package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Base Abstract/Concrete Bed Entity.
 * Demonstrates Inheritance, Polymorphism, and Encapsulation.
 */
@Entity
@Table(name = "beds")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "bed_type", discriminatorType = DiscriminatorType.STRING)
public class Bed implements Allocatable {

    @Id
    @Column(name = "bed_id", length = 20)
    private String bedId;

    @Column(name = "bed_number", nullable = false, length = 20)
    private String bedNumber;

    @Column(name = "bed_type", insertable = false, updatable = false)
    private String bedType;

    @Column(name = "hospital_id", nullable = false, length = 20)
    private String hospitalId;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE

    @Column(name = "current_patient_id", length = 20)
    private String currentPatientId;

    @Column(name = "current_visit_id", length = 20)
    private String currentVisitId;

    @Column(name = "floor_ward", nullable = false, length = 50)
    private String floorWard;

    @Column(name = "daily_charge")
    private Double dailyCharge;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Bed() {}

    public Bed(String bedId, String bedNumber, String hospitalId, String status, String floorWard, Double dailyCharge) {
        this.bedId = bedId;
        this.bedNumber = bedNumber;
        this.hospitalId = hospitalId;
        this.status = status != null ? status : "AVAILABLE";
        this.floorWard = floorWard;
        this.dailyCharge = dailyCharge != null ? dailyCharge : 0.0;
    }

    // Polymorphic method
    public boolean supportsRequirement(String requiredBedType) {
        if (requiredBedType == null || requiredBedType.equalsIgnoreCase("NO_BED")) {
            return false;
        }
        return this.getBedType() != null && this.getBedType().equalsIgnoreCase(requiredBedType);
    }

    public boolean checkAvailability() {
        return "AVAILABLE".equalsIgnoreCase(this.status);
    }

    @Override
    public boolean isAvailable() {
        return checkAvailability();
    }

    @Override
    public void allocate(String patientId, String visitId) {
        this.status = "OCCUPIED";
        this.currentPatientId = patientId;
        this.currentVisitId = visitId;
    }

    @Override
    public void release() {
        this.status = "AVAILABLE";
        this.currentPatientId = null;
        this.currentVisitId = null;
    }

    @Override
    public String getResourceIdentifier() {
        return this.bedId;
    }

    @Override
    public String getResourceCategory() {
        return "BED_" + (bedType != null ? bedType : "GENERAL");
    }

    // Getters and Setters
    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentPatientId() { return currentPatientId; }
    public void setCurrentPatientId(String currentPatientId) { this.currentPatientId = currentPatientId; }

    public String getCurrentVisitId() { return currentVisitId; }
    public void setCurrentVisitId(String currentVisitId) { this.currentVisitId = currentVisitId; }

    public String getFloorWard() { return floorWard; }
    public void setFloorWard(String floorWard) { this.floorWard = floorWard; }

    public Double getDailyCharge() { return dailyCharge; }
    public void setDailyCharge(Double dailyCharge) { this.dailyCharge = dailyCharge; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
