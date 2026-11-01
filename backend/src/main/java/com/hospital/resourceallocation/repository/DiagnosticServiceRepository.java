package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.DiagnosticService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosticServiceRepository extends JpaRepository<DiagnosticService, String> {
    List<DiagnosticService> findByHospitalId(String hospitalId);
    List<DiagnosticService> findByHospitalIdAndIsAvailableTrue(String hospitalId);
}
