package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete Bed Subclass: Intensive Care Unit (ICU) Bed
 * Equipped with specialized telemetry, ventilator, and central oxygen connections.
 */
@Entity
@DiscriminatorValue("ICU")
public class ICUBed extends Bed {

    public ICUBed() {
        super();
        setBedType("ICU");
    }

    public ICUBed(String bedId, String bedNumber, String hospitalId, String status, String floorWard, Double dailyCharge) {
        super(bedId, bedNumber, hospitalId, status, floorWard, dailyCharge);
        setBedType("ICU");
    }

    @Override
    public boolean supportsRequirement(String requiredBedType) {
        return "ICU".equalsIgnoreCase(requiredBedType);
    }

    @Override
    public String getResourceCategory() {
        return "BED_ICU";
    }
}
