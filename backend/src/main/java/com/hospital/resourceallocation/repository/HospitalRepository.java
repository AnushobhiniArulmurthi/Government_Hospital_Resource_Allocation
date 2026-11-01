package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, String> {
    Optional<Hospital> findByIsPrimaryTrue();
    List<Hospital> findByIsPrimaryFalse();
    Optional<Hospital> findByLocationName(String locationName);
}
