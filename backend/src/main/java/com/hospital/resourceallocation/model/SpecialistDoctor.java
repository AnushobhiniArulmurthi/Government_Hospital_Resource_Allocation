package com.hospital.resourceallocation.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;

/**
 * Concrete Subclass: Specialist Doctor
 * Higher level specialty care (Cardiology, Pulmonology, Neurology, Orthopedics,
 * Emergency & Critical Care, Nephrology, General Surgery).
 */
@Entity
@DiscriminatorValue("SPECIALIST")
public class SpecialistDoctor extends Doctor {

    public SpecialistDoctor() {
        super();
        setDoctorType("SPECIALIST");
    }

    public SpecialistDoctor(String doctorId, String doctorName, String specialization, String qualification,
                            String contactNumber, String email, String dutyStatus, String availabilityStatus,
                            String hospitalId, LocalDate dateOfJoining, Integer maxInpatientCapacity) {
        super(doctorId, doctorName, specialization, qualification, contactNumber, email, dutyStatus,
              availabilityStatus, hospitalId, dateOfJoining, maxInpatientCapacity);
        setDoctorType("SPECIALIST");
    }

    @Override
    public boolean matchesSpecialization(String requiredSpec) {
        if (requiredSpec == null || requiredSpec.trim().isEmpty() || "Any".equalsIgnoreCase(requiredSpec)) {
            return true;
        }
        return getSpecialization() != null && getSpecialization().equalsIgnoreCase(requiredSpec.trim());
    }

    @Override
    public String getResourceCategory() {
        return "DOCTOR_SPECIALIST_" + (getSpecialization() != null ? getSpecialization().replace(" ", "_").toUpperCase() : "GENERAL");
    }
}
