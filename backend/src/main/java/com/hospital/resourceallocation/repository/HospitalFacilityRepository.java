package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.HospitalFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalFacilityRepository extends JpaRepository<HospitalFacility, String> {
    List<HospitalFacility> findByHospitalId(String hospitalId);
}
