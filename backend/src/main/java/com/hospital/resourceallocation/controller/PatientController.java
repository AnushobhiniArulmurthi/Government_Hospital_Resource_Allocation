package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.*;
import com.hospital.resourceallocation.model.Patient;
import com.hospital.resourceallocation.model.PatientVisit;
import com.hospital.resourceallocation.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Patient Management REST Controller
 * Supports intake registration, clinical triage assessment,
 * doctor preference and alternative matching, and Unified Patient 360 record retrieval.
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ApiResponse<List<Patient>> getAllPatients() {
        return ApiResponse.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ApiResponse<Patient> getPatientById(@PathVariable String id) {
        return patientService.getPatientById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("Patient not found: " + id));
    }

    @GetMapping("/{id}/profile")
    public ApiResponse<PatientProfileDTO> getPatientProfile(@PathVariable String id) {
        return ApiResponse.ok(patientService.getPatientProfile(id));
    }

    @GetMapping("/{id}/visits")
    public ApiResponse<List<PatientVisit>> getPatientVisits(@PathVariable String id) {
        return ApiResponse.ok(patientService.getVisitsByPatientId(id));
    }

    @PostMapping
    public ApiResponse<Patient> savePatientDetails(@Valid @RequestBody PatientRegistrationDTO dto) {
        Patient patient = patientService.savePatient(dto);
        return ApiResponse.ok("Patient details saved successfully with Citizen ID: " + patient.getPatientId(), patient);
    }

    @PostMapping("/register")
    public ApiResponse<PatientVisit> registerPatient(@Valid @RequestBody PatientRegistrationDTO dto) {
        PatientVisit visit = patientService.registerPatientVisit(dto);
        return ApiResponse.ok("Patient visit registered successfully with Visit ID: " + visit.getVisitId() + 
                ". Status: PENDING_TRIAGE (awaiting medical assessment)", visit);
    }

    @PostMapping("/regular-visit")
    public ApiResponse<DoctorAssignmentDecisionDTO> registerRegularPatientVisit(@RequestBody Map<String, Object> body) {
        String patientId = (String) body.get("patientId");
        if (patientId == null || patientId.trim().isEmpty()) {
            return ApiResponse.error("Patient ID is mandatory for regular patient intake.");
        }
        String category = (String) body.get("patientCategory");
        String specialization = (String) body.get("preferredSpecialization");
        String notes = (String) body.get("notes");
        String preferredDoctorId = (String) body.get("preferredDoctorId");

        DoctorAssignmentDecisionDTO decision = patientService.registerRegularPatientVisit(
                patientId, category, specialization, notes, preferredDoctorId
        );
        return ApiResponse.ok(decision.getMessage(), decision);
    }

    @PostMapping("/visits/{visitId}/triage")
    public ApiResponse<DoctorAssignmentDecisionDTO> assignTriageSeverity(@PathVariable String visitId,
                                                                        @Valid @RequestBody TriageAssessmentDTO triageDTO) {
        DoctorAssignmentDecisionDTO decision = patientService.performTriageAndAssignDoctor(visitId, triageDTO);
        return ApiResponse.ok("Clinical triage severity assessed: " + triageDTO.getSeverityLevel() + 
                ". " + decision.getMessage(), decision);
    }

    @PostMapping("/visits/{visitId}/doctor-response")
    public ApiResponse<DoctorAssignmentDecisionDTO> respondToAlternativeDoctor(@PathVariable String visitId,
                                                                               @RequestBody Map<String, Object> body) {
        boolean accepted = Boolean.parseBoolean(String.valueOf(body.get("accepted")));
        String selectedDoctorId = body.get("selectedDoctorId") != null ? String.valueOf(body.get("selectedDoctorId")) : null;
        DoctorAssignmentDecisionDTO decision = patientService.respondToAlternativeDoctor(visitId, accepted, selectedDoctorId);
        return ApiResponse.ok(decision.getMessage(), decision);
    }

    @GetMapping("/visits")
    public ApiResponse<List<PatientVisit>> getAllVisits() {
        return ApiResponse.ok(patientService.getAllVisits());
    }

    @GetMapping("/visits/{visitId}")
    public ApiResponse<PatientVisit> getVisitById(@PathVariable String visitId) {
        return patientService.getVisitById(visitId)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("Visit not found: " + visitId));
    }
}
