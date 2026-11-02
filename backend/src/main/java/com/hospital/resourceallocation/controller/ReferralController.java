package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.algorithms.RouteResult;
import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.dto.ReferralRequestDTO;
import com.hospital.resourceallocation.model.Hospital;
import com.hospital.resourceallocation.model.Referral;
import com.hospital.resourceallocation.service.ReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Referral Management and Dijkstra Shortest-Path Route REST Controller
 */
@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    private final ReferralService referralService;

    @Autowired
    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @GetMapping
    public ApiResponse<List<Referral>> getAllReferrals() {
        return ApiResponse.ok(referralService.getAllReferrals());
    }

    @GetMapping("/details")
    public ApiResponse<List<com.hospital.resourceallocation.dto.ReferralDetailDTO>> getEnrichedReferrals() {
        return ApiResponse.ok(referralService.getEnrichedReferrals());
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Referral> approveReferral(@PathVariable String id) {
        Referral approved = referralService.approveReferral(id);
        return ApiResponse.ok("Referral " + id + " verified and approved for inter-hospital transfer dispatch.", approved);
    }

    @PostMapping("/{id}/move-to-waiting-queue")
    public ApiResponse<String> moveReferralToWaitingQueue(@PathVariable String id) {
        referralService.moveReferralToWaitingQueue(id);
        return ApiResponse.ok("Patient from referral " + id + " moved to Severity-Based Priority Queue.", "Moved to queue successfully");
    }

    @GetMapping("/hospitals")
    public ApiResponse<List<Hospital>> getAllHospitals() {
        return ApiResponse.ok(referralService.getAllHospitals());
    }

    @PostMapping("/find-fastest")
    public ApiResponse<RouteResult> findFastestHospital(@RequestBody ReferralRequestDTO dto) {
        String fromHospId = dto.getFromHospitalId() != null ? dto.getFromHospitalId() : "H-001";
        RouteResult route = referralService.findFastestEligibleHospital(
                fromHospId, dto.getRequiredBedType(), dto.getRequiredSpecialization(), dto.getRequiredResources()
        );
        if (route == null) {
            return ApiResponse.error("No eligible referral hospital satisfies the complete mandatory requirement.");
        }
        return ApiResponse.ok("Fastest eligible referral hospital route calculated via Dijkstra's algorithm", route);
    }

    @PostMapping("/process")
    public ApiResponse<Referral> processReferral(@RequestBody ReferralRequestDTO dto) {
        Referral referral = referralService.processReferral(dto);
        return ApiResponse.ok("Referral confirmed to hospital with ID: " + referral.getReferralId(), referral);
    }
}
