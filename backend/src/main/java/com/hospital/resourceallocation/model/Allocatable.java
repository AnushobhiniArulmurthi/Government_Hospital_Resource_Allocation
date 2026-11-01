package com.hospital.resourceallocation.model;

/**
 * Abstraction Interface: Represents any hospital asset (bed, equipment, facility, doctor)
 * that can be dynamically allocated to a patient and subsequently released.
 */
public interface Allocatable {

    boolean isAvailable();

    void allocate(String patientId, String visitId);

    void release();

    String getResourceIdentifier();

    String getResourceCategory();
}
