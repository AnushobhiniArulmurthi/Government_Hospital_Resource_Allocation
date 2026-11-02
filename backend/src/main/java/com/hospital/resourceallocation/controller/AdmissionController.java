package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.AdmissionRequestDTO;
import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.dto.DischargeRequestDTO;
import com.hospital.resourceallocation.model.Admission;
import com.hospital.resourceallocation.model.DischargeHistory;
import com.hospital.resourceallocation.service.AdmissionDischargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Hospital Inpatient Admission and Discharge REST Controller
 */
@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {

    private final AdmissionDischargeService admissionService;

    @Autowired
    public AdmissionController(AdmissionDischargeService admissionService) {
        this.admissionService = admissionService;
    }

    @GetMapping
    public ApiResponse<List<Admission>> getAllAdmissions() {
        return ApiResponse.ok(admissionService.getAllAdmissions());
    }

    @GetMapping("/active")
    public ApiResponse<List<Admission>> getActiveAdmissions() {
        return ApiResponse.ok(admissionService.getActiveAdmissions());
    }

    @GetMapping("/admitted-patients")
    public ApiResponse<List<com.hospital.resourceallocation.dto.AdmittedPatientDTO>> getAdmittedPatients() {
        return ApiResponse.ok(admissionService.getAdmittedPatients());
    }

    @GetMapping("/search-admitted")
    public ApiResponse<com.hospital.resourceallocation.dto.AdmittedPatientDTO> searchAdmittedPatient(@RequestParam String query) {
        return ApiResponse.ok(admissionService.searchAdmittedPatient(query));
    }

    @PostMapping("/admit")
    public ApiResponse<Admission> admitPatient(@RequestBody AdmissionRequestDTO dto) {
        Admission admission = admissionService.admitPatient(dto);
        return ApiResponse.ok("Patient admitted successfully with Admission ID: " + admission.getAdmissionId(), admission);
    }

    @PostMapping("/discharge")
    public ApiResponse<DischargeHistory> dischargePatient(@RequestBody DischargeRequestDTO dto) {
        DischargeHistory history = admissionService.dischargePatient(dto);
        return ApiResponse.ok("Patient successfully discharged. Resources released and waiting queues updated.", history);
    }

    @GetMapping("/discharge-history")
    public ApiResponse<List<DischargeHistory>> getDischargeHistory() {
        return ApiResponse.ok(admissionService.getAllDischargeHistory());
    }
}
