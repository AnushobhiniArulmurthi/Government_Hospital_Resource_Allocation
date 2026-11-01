package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.model.DiagnosticCentre;
import com.hospital.resourceallocation.model.DiagnosticOrder;
import com.hospital.resourceallocation.model.DiagnosticService;
import com.hospital.resourceallocation.service.DiagnosticManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Diagnostic Services and External Diagnostic Centres REST Controller
 */
@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticController {

    private final DiagnosticManagementService diagnosticService;

    @Autowired
    public DiagnosticController(DiagnosticManagementService diagnosticService) {
        this.diagnosticService = diagnosticService;
    }

    @GetMapping("/services")
    public ApiResponse<List<DiagnosticService>> getServices(@RequestParam(defaultValue = "H-001") String hospitalId) {
        return ApiResponse.ok(diagnosticService.getServicesByHospital(hospitalId));
    }

    @GetMapping("/centres")
    public ApiResponse<List<DiagnosticCentre>> getDiagnosticCentres() {
        return ApiResponse.ok(diagnosticService.getAllDiagnosticCentres());
    }

    @GetMapping("/orders")
    public ApiResponse<List<DiagnosticOrder>> getAllOrders(@RequestParam(required = false) String visitId) {
        if (visitId != null && !visitId.trim().isEmpty()) {
            return ApiResponse.ok(diagnosticService.getOrdersByVisit(visitId));
        }
        return ApiResponse.ok(diagnosticService.getAllOrders());
    }

    @PostMapping("/orders")
    public ApiResponse<DiagnosticOrder> createOrder(@RequestBody Map<String, String> body) {
        String visitId = body.get("visitId");
        String patientId = body.get("patientId");
        String doctorId = body.get("doctorId");
        String testType = body.get("testType");
        String priority = body.get("priority");
        String centreId = body.get("diagnosticCentreId");

        DiagnosticOrder order = diagnosticService.createDiagnosticOrder(visitId, patientId, doctorId, testType, priority, centreId);
        return ApiResponse.ok("Diagnostic test order created successfully: " + order.getOrderId(), order);
    }

    @PostMapping("/orders/{orderId}/schedule")
    public ApiResponse<DiagnosticOrder> scheduleOrder(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        String centreId = body.get("diagnosticCentreId");
        DiagnosticOrder order = diagnosticService.scheduleOrder(orderId, LocalDateTime.now().plusHours(1), centreId);
        return ApiResponse.ok("Diagnostic order scheduled", order);
    }

    @PostMapping("/orders/{orderId}/start")
    public ApiResponse<DiagnosticOrder> startOrder(@PathVariable String orderId) {
        DiagnosticOrder order = diagnosticService.startOrder(orderId);
        return ApiResponse.ok("Diagnostic test started (in progress)", order);
    }

    @PostMapping("/orders/{orderId}/complete")
    public ApiResponse<DiagnosticOrder> completeTest(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        String resultsSummary = body.get("resultsSummary");
        DiagnosticOrder order = diagnosticService.completeTest(orderId, resultsSummary);
        return ApiResponse.ok("Diagnostic test completed. Report ready for doctor review.", order);
    }

    @PostMapping("/orders/{orderId}/review")
    public ApiResponse<DiagnosticOrder> reviewReport(@PathVariable String orderId, @RequestBody Map<String, String> body) {
        String reviewNotes = body.get("doctorReviewNotes");
        String doctorId = body.get("doctorId");
        DiagnosticOrder order = diagnosticService.reviewReport(orderId, reviewNotes, doctorId);
        return ApiResponse.ok("Diagnostic report reviewed by doctor", order);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResponse<DiagnosticOrder> cancelOrder(@PathVariable String orderId, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        DiagnosticOrder order = diagnosticService.cancelOrder(orderId, reason);
        return ApiResponse.ok("Diagnostic order cancelled", order);
    }

    @PostMapping("/centres")
    public ApiResponse<DiagnosticCentre> addCentre(@RequestBody DiagnosticCentre centre) {
        return ApiResponse.ok("Diagnostic centre added successfully", diagnosticService.addDiagnosticCentre(centre));
    }
}
