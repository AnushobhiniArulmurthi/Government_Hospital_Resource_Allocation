package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.DiagnosticOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticOrderRepository extends JpaRepository<DiagnosticOrder, String> {
    List<DiagnosticOrder> findByVisitId(String visitId);
    List<DiagnosticOrder> findByPatientId(String patientId);
    List<DiagnosticOrder> findByStatus(String status);
}
