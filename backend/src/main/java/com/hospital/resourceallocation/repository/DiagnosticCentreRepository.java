package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.DiagnosticCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticCentreRepository extends JpaRepository<DiagnosticCentre, String> {
    List<DiagnosticCentre> findByIsActiveTrue();
    List<DiagnosticCentre> findByAvailableServicesContainingIgnoreCase(String testName);
}
