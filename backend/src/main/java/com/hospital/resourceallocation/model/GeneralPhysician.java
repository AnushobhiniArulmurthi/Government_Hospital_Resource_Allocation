package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;

/**
 * Concrete Subclass: General Physician
 * First line of clinical care; handles broad diagnosis, routine outpatient visits,
 * basic medication protocols, and triage routing.
 */
@Entity
@DiscriminatorValue("GENERAL_PHYSICIAN")
public class GeneralPhysician extends Doctor {

    public GeneralPhysician() {
        super();
        setDoctorType("GENERAL_PHYSICIAN");
    }

    public GeneralPhysician(String doctorId, String doctorName, String specialization, String qualification,
                            String contactNumber, String email, String dutyStatus, String availabilityStatus,
                            String hospitalId, LocalDate dateOfJoining, Integer maxInpatientCapacity) {
        super(doctorId, doctorName, specialization, qualification, contactNumber, email, dutyStatus,
              availabilityStatus, hospitalId, dateOfJoining, maxInpatientCapacity);
        setDoctorType("GENERAL_PHYSICIAN");
    }

    @Override
    public boolean matchesSpecialization(String requiredSpec) {
        if (requiredSpec == null || requiredSpec.trim().isEmpty() || "Any".equalsIgnoreCase(requiredSpec)) {
            return true;
        }
        return "General Medicine".equalsIgnoreCase(requiredSpec) 
                || "General Practice".equalsIgnoreCase(requiredSpec)
                || super.matchesSpecialization(requiredSpec);
    }

    @Override
    public String getResourceCategory() {
        return "DOCTOR_GENERAL_PHYSICIAN";
    }
}
