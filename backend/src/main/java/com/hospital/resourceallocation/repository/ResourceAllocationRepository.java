package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.ResourceAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, String> {
    List<ResourceAllocation> findByVisitId(String visitId);
    List<ResourceAllocation> findByPatientId(String patientId);
    List<ResourceAllocation> findByVisitIdAndStatus(String visitId, String status);
    List<ResourceAllocation> findByResourceIdAndStatus(String resourceId, String status);
}
