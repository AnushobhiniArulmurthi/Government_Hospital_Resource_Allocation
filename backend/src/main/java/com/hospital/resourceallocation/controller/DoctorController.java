package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.dto.DoctorRegistrationDTO;
import com.hospital.resourceallocation.model.Doctor;
import com.hospital.resourceallocation.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Doctor Management REST Controller
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    @Autowired
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ApiResponse<List<Doctor>> getAllDoctors(@RequestParam(required = false) String search,
                                                   @RequestParam(required = false) String specialization,
                                                   @RequestParam(defaultValue = "false") boolean includeDeactivated) {
        if (search != null && !search.trim().isEmpty()) {
            return ApiResponse.ok(doctorService.searchByName(search));
        }
        if (specialization != null && !specialization.trim().isEmpty()) {
            return ApiResponse.ok(doctorService.filterBySpecialization(specialization));
        }
        return ApiResponse.ok(doctorService.getAllDoctors(includeDeactivated));
    }

    @GetMapping("/{id}")
    public ApiResponse<Doctor> getDoctorById(@PathVariable String id) {
        return doctorService.getDoctorById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("Doctor not found: " + id));
    }

    @GetMapping("/available")
    public ApiResponse<List<Doctor>> getAvailableDoctors(@RequestParam(defaultValue = "H-001") String hospitalId,
                                                         @RequestParam(required = false) String specialization) {
        if (specialization != null && !specialization.trim().isEmpty()) {
            return ApiResponse.ok(doctorService.findSuitableAvailableDoctors(hospitalId, specialization));
        }
        return ApiResponse.ok(doctorService.getAvailableDoctors(hospitalId));
    }

    @PostMapping
    public ApiResponse<Doctor> registerDoctor(@Valid @RequestBody DoctorRegistrationDTO dto) {
        Doctor created = doctorService.registerDoctor(dto);
        return ApiResponse.ok("Doctor registered successfully with generated ID: " + created.getDoctorId(), created);
    }

    @PutMapping("/{id}")
    public ApiResponse<Doctor> updateDoctor(@PathVariable String id, @Valid @RequestBody DoctorRegistrationDTO dto) {
        return ApiResponse.ok("Doctor updated successfully", doctorService.updateDoctor(id, dto));
    }

    @PostMapping("/{id}/toggle-duty")
    public ApiResponse<Doctor> toggleDutyStatus(@PathVariable String id) {
        Doctor updated = doctorService.toggleDutyStatus(id);
        return ApiResponse.ok("Doctor duty status updated to " + updated.getDutyStatus(), updated);
    }

    @PatchMapping("/{id}/duty")
    public ApiResponse<Doctor> updateDutyStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String duty = body.get("dutyStatus");
        return ApiResponse.ok("Duty status updated to " + duty, doctorService.updateDutyStatus(id, duty));
    }

    @PatchMapping("/{id}/availability")
    public ApiResponse<Doctor> updateAvailabilityStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String availability = body.get("availabilityStatus");
        return ApiResponse.ok("Availability status updated to " + availability, doctorService.updateAvailabilityStatus(id, availability));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Doctor> deactivateDoctor(@PathVariable String id) {
        return ApiResponse.ok("Doctor deactivated successfully. Historical patient records preserved.", doctorService.deactivateDoctor(id));
    }
}
