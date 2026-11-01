package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.PatientVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientVisitRepository extends JpaRepository<PatientVisit, String> {

    List<PatientVisit> findByPatientIdOrderByArrivalTimeDesc(String patientId);

    List<PatientVisit> findByVisitStatus(String visitStatus);

    List<PatientVisit> findBySeverityLevel(String severityLevel);

    @Query(value = "SELECT COUNT(*) FROM patient_visits WHERE severity_level = 'Critical'", nativeQuery = true)
    long countCriticalVisits();

    @Query(value = "SELECT COUNT(*) FROM patient_visits WHERE severity_level = 'Serious'", nativeQuery = true)
    long countSeriousVisits();

    @Query(value = "SELECT COUNT(*) FROM patient_visits WHERE severity_level = 'Minor'", nativeQuery = true)
    long countMinorVisits();

    @Query(value = "SELECT COUNT(*) FROM patient_visits WHERE visit_status IN ('IN_CONSULTATION', 'TREATMENT_PLANNED')", nativeQuery = true)
    long countUnderTreatment();

    @Query(value = "SELECT COUNT(*) FROM patient_visits WHERE visit_status IN ('COMPLETED', 'DISCHARGED')", nativeQuery = true)
    long countDischarged();
}
