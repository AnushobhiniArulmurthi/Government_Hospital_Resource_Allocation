package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.HospitalRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRouteRepository extends JpaRepository<HospitalRoute, Integer> {
    List<HospitalRoute> findByFromLocation(String fromLocation);
}
