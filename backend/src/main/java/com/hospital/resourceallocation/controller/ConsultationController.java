package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.dto.ConsultationRequestDTO;
import com.hospital.resourceallocation.model.Consultation;
import com.hospital.resourceallocation.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Doctor Consultation REST Controller
 */
@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    @Autowired
    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping
    public ApiResponse<List<Consultation>> getAllConsultations() {
        return ApiResponse.ok(consultationService.getAllConsultations());
    }

    @GetMapping("/visit/{visitId}")
    public ApiResponse<Consultation> getConsultationByVisit(@PathVariable String visitId) {
        return consultationService.getConsultationByVisitId(visitId)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("No consultation record found for visit: " + visitId));
    }

    @PostMapping("/assign")
    public ApiResponse<Consultation> assignDoctor(@RequestBody Map<String, String> body) {
        String visitId = body.get("visitId");
        String doctorId = body.get("doctorId");
        Consultation consultation = consultationService.assignDoctor(visitId, doctorId);
        return ApiResponse.ok("Doctor assigned and consultation initiated: " + consultation.getConsultationId(), consultation);
    }

    @PostMapping("/update")
    public ApiResponse<Consultation> updateConsultation(@RequestBody ConsultationRequestDTO dto) {
        Consultation updated = consultationService.updateConsultation(dto);
        return ApiResponse.ok("Consultation record updated successfully", updated);
    }
}
