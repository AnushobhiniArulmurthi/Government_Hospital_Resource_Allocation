package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete Subclass: Medical Supplies (Consumables)
 * Items such as IV fluids, syringes, first aid kits, medicines that are deducted from inventory upon use.
 */
@Entity
@DiscriminatorValue("SUPPLY")
public class MedicalSupply extends Resource {

    public MedicalSupply() {
        super();
        setCategory("SUPPLY");
        setIsReusable(false);
    }

    public MedicalSupply(String resourceId, String resourceName, String hospitalId,
                         Integer totalQuantity, Integer availableQuantity) {
        super(resourceId, resourceName, "SUPPLY", hospitalId, totalQuantity, availableQuantity, false);
        setIsReusable(false);
    }

    @Override
    public boolean checkAvailability(int units) {
        return super.checkAvailability(units);
    }

    @Override
    public String getResourceCategory() {
        return "SUPPLY";
    }
}
