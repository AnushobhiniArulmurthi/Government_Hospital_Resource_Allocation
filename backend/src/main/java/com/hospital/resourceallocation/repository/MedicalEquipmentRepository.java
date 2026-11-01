package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.MedicalEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalEquipmentRepository extends JpaRepository<MedicalEquipment, String> {
    List<MedicalEquipment> findByHospitalId(String hospitalId);
    List<MedicalEquipment> findByHospitalIdAndStatus(String hospitalId, String status);
}
