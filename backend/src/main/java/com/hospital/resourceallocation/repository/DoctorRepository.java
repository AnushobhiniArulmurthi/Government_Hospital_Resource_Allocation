package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String> {

    List<Doctor> findByIsActiveTrue();

    List<Doctor> findByHospitalId(String hospitalId);

    List<Doctor> findByIsActiveTrueAndHospitalId(String hospitalId);

    List<Doctor> findByIsActiveTrueAndSpecializationContainingIgnoreCase(String specialization);

    List<Doctor> findByIsActiveTrueAndDoctorNameContainingIgnoreCase(String name);

    List<Doctor> findByHospitalIdAndDutyStatusAndAvailabilityStatus(String hospitalId, String dutyStatus, String availabilityStatus);

    List<Doctor> findByIsActiveTrueAndHospitalIdAndDutyStatusAndAvailabilityStatus(String hospitalId, String dutyStatus, String availabilityStatus);

    List<Doctor> findByHospitalIdAndSpecializationAndDutyStatusAndAvailabilityStatus(
            String hospitalId, String specialization, String dutyStatus, String availabilityStatus);

    List<Doctor> findByIsActiveTrueAndHospitalIdAndSpecializationAndDutyStatusAndAvailabilityStatus(
            String hospitalId, String specialization, String dutyStatus, String availabilityStatus);

    @Query(value = "SELECT current_val FROM doctor_id_sequence WHERE id = 1 FOR UPDATE", nativeQuery = true)
    Long getNextSequenceValForUpdate();

    @Modifying
    @Query(value = "UPDATE doctor_id_sequence SET current_val = current_val + 1 WHERE id = 1", nativeQuery = true)
    int incrementDoctorSequence();
}
