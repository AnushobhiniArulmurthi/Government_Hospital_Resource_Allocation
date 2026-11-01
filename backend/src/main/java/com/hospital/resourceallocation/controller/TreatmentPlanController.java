package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.dto.TreatmentPlanRequestDTO;
import com.hospital.resourceallocation.model.TreatmentPlan;
import com.hospital.resourceallocation.service.TreatmentPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Treatment Plan REST Controller
 */
@RestController
@RequestMapping("/api/treatment-plans")
public class TreatmentPlanController {

    private final TreatmentPlanService treatmentPlanService;

    @Autowired
    public TreatmentPlanController(TreatmentPlanService treatmentPlanService) {
        this.treatmentPlanService = treatmentPlanService;
    }

    @GetMapping
    public ApiResponse<List<TreatmentPlan>> getAllTreatmentPlans() {
        return ApiResponse.ok(treatmentPlanService.getAllTreatmentPlans());
    }

    @GetMapping("/visit/{visitId}")
    public ApiResponse<TreatmentPlan> getTreatmentPlanByVisit(@PathVariable String visitId) {
        return treatmentPlanService.getTreatmentPlanByVisitId(visitId)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("No treatment plan found for visit: " + visitId));
    }

    @PostMapping
    public ApiResponse<TreatmentPlan> createOrUpdateTreatmentPlan(@RequestBody TreatmentPlanRequestDTO dto) {
        TreatmentPlan plan = treatmentPlanService.createOrUpdateTreatmentPlan(dto);
        return ApiResponse.ok("Treatment plan created/updated successfully with ID: " + plan.getTreatmentPlanId(), plan);
    }
}
