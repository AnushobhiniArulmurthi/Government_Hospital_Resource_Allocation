package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete Subclass: Hospital Facility
 * Facilities such as Emergency Room, Operation Theatre, Laboratory, Pharmacy, Ambulance.
 */
@Entity
@DiscriminatorValue("FACILITY")
public class HospitalFacility extends Resource {

    public HospitalFacility() {
        super();
        setCategory("FACILITY");
        setIsReusable(true);
    }

    public HospitalFacility(String resourceId, String resourceName, String hospitalId,
                            Integer totalQuantity, Integer availableQuantity) {
        super(resourceId, resourceName, "FACILITY", hospitalId, totalQuantity, availableQuantity, true);
        setIsReusable(true);
    }

    @Override
    public boolean checkAvailability(int units) {
        return super.checkAvailability(units);
    }

    @Override
    public String getResourceCategory() {
        return "FACILITY";
    }
}
