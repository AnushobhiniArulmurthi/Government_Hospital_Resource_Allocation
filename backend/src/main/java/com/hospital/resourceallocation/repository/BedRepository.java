package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Bed;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BedRepository extends JpaRepository<Bed, String> {

    List<Bed> findByHospitalId(String hospitalId);

    List<Bed> findByHospitalIdAndStatus(String hospitalId, String status);

    List<Bed> findByHospitalIdAndBedTypeAndStatus(String hospitalId, String bedType, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bed b WHERE b.bedId = :bedId")
    Optional<Bed> findByIdForUpdate(@Param("bedId") String bedId);

    @Query(value = "SELECT COUNT(*) FROM beds WHERE hospital_id = :hospitalId AND status = 'AVAILABLE'", nativeQuery = true)
    long countAvailableBeds(@Param("hospitalId") String hospitalId);

    @Query(value = "SELECT COUNT(*) FROM beds WHERE hospital_id = :hospitalId AND status = 'OCCUPIED'", nativeQuery = true)
    long countOccupiedBeds(@Param("hospitalId") String hospitalId);

    @Query(value = "SELECT COUNT(*) FROM beds WHERE hospital_id = :hospitalId AND bed_type = :bedType AND status = 'AVAILABLE'", nativeQuery = true)
    long countAvailableByType(@Param("hospitalId") String hospitalId, @Param("bedType") String bedType);
}
