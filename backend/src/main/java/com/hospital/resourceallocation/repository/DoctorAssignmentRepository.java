package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.DoctorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorAssignmentRepository extends JpaRepository<DoctorAssignment, String> {
    List<DoctorAssignment> findByDoctorIdAndStatus(String doctorId, String status);
    List<DoctorAssignment> findByVisitId(String visitId);
}
