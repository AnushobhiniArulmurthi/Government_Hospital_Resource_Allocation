package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete Bed Subclass: General Bed
 * Represents standard ward beds for recovering or non-critical inpatients.
 */
@Entity
@DiscriminatorValue("GENERAL")
public class GeneralBed extends Bed {

    public GeneralBed() {
        super();
        setBedType("GENERAL");
    }

    public GeneralBed(String bedId, String bedNumber, String hospitalId, String status, String floorWard, Double dailyCharge) {
        super(bedId, bedNumber, hospitalId, status, floorWard, dailyCharge);
        setBedType("GENERAL");
    }

    @Override
    public boolean supportsRequirement(String requiredBedType) {
        return "GENERAL".equalsIgnoreCase(requiredBedType);
    }

    @Override
    public String getResourceCategory() {
        return "BED_GENERAL";
    }
}
