package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, String> {
    List<Referral> findByStatus(String status);
    List<Referral> findByPatientId(String patientId);
    List<Referral> findByFromHospitalId(String fromHospitalId);
    List<Referral> findByToHospitalId(String toHospitalId);

    @Query(value = "SELECT COUNT(*) FROM referrals WHERE status = 'PENDING'", nativeQuery = true)
    long countPendingReferrals();

    @Query(value = "SELECT COUNT(*) FROM referrals WHERE status IN ('CONFIRMED', 'COMPLETED')", nativeQuery = true)
    long countCompletedReferrals();
}
