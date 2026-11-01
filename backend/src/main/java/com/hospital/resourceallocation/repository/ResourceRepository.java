package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Resource;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {

    List<Resource> findByHospitalId(String hospitalId);

    List<Resource> findByHospitalIdAndCategory(String hospitalId, String category);

    Optional<Resource> findByHospitalIdAndResourceNameIgnoreCase(String hospitalId, String resourceName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resource r WHERE r.resourceId = :resourceId")
    Optional<Resource> findByIdForUpdate(@Param("resourceId") String resourceId);

    @Query(value = "SELECT COUNT(*) FROM resources WHERE hospital_id = :hospitalId AND category = 'EQUIPMENT' AND available_quantity > 0", nativeQuery = true)
    long countAvailableEquipment(@Param("hospitalId") String hospitalId);

    @Query(value = "SELECT SUM(available_quantity) FROM resources WHERE hospital_id = :hospitalId AND category = 'SUPPLY'", nativeQuery = true)
    Long sumMedicalSupplies(@Param("hospitalId") String hospitalId);
}
