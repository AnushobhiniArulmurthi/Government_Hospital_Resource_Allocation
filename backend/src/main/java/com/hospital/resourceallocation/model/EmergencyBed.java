package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete Bed Subclass: Emergency Trauma Bed
 * Utilized for high-turnaround triage, crash cart resuscitation, and stabilization.
 */
@Entity
@DiscriminatorValue("EMERGENCY")
public class EmergencyBed extends Bed {

    public EmergencyBed() {
        super();
        setBedType("EMERGENCY");
    }

    public EmergencyBed(String bedId, String bedNumber, String hospitalId, String status, String floorWard, Double dailyCharge) {
        super(bedId, bedNumber, hospitalId, status, floorWard, dailyCharge);
        setBedType("EMERGENCY");
    }

    @Override
    public boolean supportsRequirement(String requiredBedType) {
        return "EMERGENCY".equalsIgnoreCase(requiredBedType);
    }

    @Override
    public String getResourceCategory() {
        return "BED_EMERGENCY";
    }
}
