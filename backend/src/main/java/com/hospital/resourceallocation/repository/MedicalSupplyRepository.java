package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.MedicalSupply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalSupplyRepository extends JpaRepository<MedicalSupply, String> {
    List<MedicalSupply> findByHospitalId(String hospitalId);
}
