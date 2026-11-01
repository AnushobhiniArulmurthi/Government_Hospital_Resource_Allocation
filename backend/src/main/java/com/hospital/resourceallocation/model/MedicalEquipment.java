package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete Subclass: Medical Equipment
 * Reusable life support and monitoring devices (Ventilators, Oxygen Cylinders, ECGs, Monitors, etc.)
 */
@Entity
@DiscriminatorValue("EQUIPMENT")
public class MedicalEquipment extends Resource {

    public MedicalEquipment() {
        super();
        setCategory("EQUIPMENT");
        setIsReusable(true);
    }

    public MedicalEquipment(String resourceId, String resourceName, String hospitalId,
                            Integer totalQuantity, Integer availableQuantity) {
        super(resourceId, resourceName, "EQUIPMENT", hospitalId, totalQuantity, availableQuantity, true);
        setIsReusable(true);
    }

    @Override
    public boolean checkAvailability(int units) {
        return super.checkAvailability(units);
    }

    @Override
    public String getResourceCategory() {
        return "EQUIPMENT";
    }
}
