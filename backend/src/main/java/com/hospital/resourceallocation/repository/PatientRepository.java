package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    List<Patient> findByFullNameContainingIgnoreCase(String name);
    List<Patient> findByContactNumber(String contactNumber);

    @Query(value = "SELECT COUNT(*) FROM patients", nativeQuery = true)
    long countTotalPatients();
}
