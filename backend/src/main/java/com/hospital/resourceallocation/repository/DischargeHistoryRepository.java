package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.DischargeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DischargeHistoryRepository extends JpaRepository<DischargeHistory, String> {
    List<DischargeHistory> findAllByOrderByDischargeDateDesc();
    List<DischargeHistory> findByPatientIdOrderByDischargeDateDesc(String patientId);
}
