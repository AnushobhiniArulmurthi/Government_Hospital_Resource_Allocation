package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, String> {
    Optional<Consultation> findByVisitId(String visitId);
    List<Consultation> findByPatientId(String patientId);
    List<Consultation> findByDoctorId(String doctorId);
    List<Consultation> findByStatus(String status);
}
