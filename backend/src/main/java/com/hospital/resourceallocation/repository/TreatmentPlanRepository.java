package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, String> {
    Optional<TreatmentPlan> findByVisitId(String visitId);
    Optional<TreatmentPlan> findByConsultationId(String consultationId);
    List<TreatmentPlan> findByPatientId(String patientId);
    List<TreatmentPlan> findByDoctorId(String doctorId);
}
