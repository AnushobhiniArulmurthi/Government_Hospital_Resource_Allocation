package com.hospital.resourceallocation.model;

import java.util.List;

/**
 * Abstraction Interface: Defines policy for validating and reserving mandatory and optional resources.
 */
public interface ResourceAllocationPolicy {

    boolean validateAvailability(List<String> requiredResources, Hospital hospital);

    boolean supportsRequirement(String resourceType, String patientCondition);
}
