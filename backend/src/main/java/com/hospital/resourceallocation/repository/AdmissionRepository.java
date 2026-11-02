package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, String> {
    Optional<Admission> findByVisitId(String visitId);
    List<Admission> findByPatientId(String patientId);
    List<Admission> findByPatientIdAndStatus(String patientId, String status);
    List<Admission> findByStatus(String status);
    List<Admission> findByHospitalIdAndStatus(String hospitalId, String status);

    @Query(value = "SELECT COUNT(*) FROM admissions WHERE status = 'ACTIVE'", nativeQuery = true)
    long countActiveAdmissions();
}
