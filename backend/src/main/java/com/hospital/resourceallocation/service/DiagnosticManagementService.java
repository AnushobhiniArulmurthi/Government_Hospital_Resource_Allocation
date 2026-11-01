package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.model.DiagnosticCentre;
import com.hospital.resourceallocation.model.DiagnosticOrder;
import com.hospital.resourceallocation.model.DiagnosticService;
import com.hospital.resourceallocation.repository.DiagnosticCentreRepository;
import com.hospital.resourceallocation.repository.DiagnosticOrderRepository;
import com.hospital.resourceallocation.repository.DiagnosticServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Diagnostic Service & External Centre Management Service
 */
@Service
public class DiagnosticManagementService {

    private final DiagnosticServiceRepository diagnosticServiceRepository;
    private final DiagnosticCentreRepository diagnosticCentreRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;

    @Autowired
    public DiagnosticManagementService(DiagnosticServiceRepository diagnosticServiceRepository,
                                       DiagnosticCentreRepository diagnosticCentreRepository,
                                       DiagnosticOrderRepository diagnosticOrderRepository) {
        this.diagnosticServiceRepository = diagnosticServiceRepository;
        this.diagnosticCentreRepository = diagnosticCentreRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
    }

    public List<DiagnosticService> getAllInternalServices() {
        return diagnosticServiceRepository.findAll();
    }

    public List<DiagnosticService> getServicesByHospital(String hospitalId) {
        return diagnosticServiceRepository.findByHospitalId(hospitalId);
    }

    public List<DiagnosticCentre> getAllDiagnosticCentres() {
        return diagnosticCentreRepository.findAll();
    }

    public List<DiagnosticOrder> getAllOrders() {
        return diagnosticOrderRepository.findAll();
    }

    public List<DiagnosticOrder> getOrdersByVisit(String visitId) {
        return diagnosticOrderRepository.findByVisitId(visitId);
    }

    @Transactional
    public DiagnosticOrder createDiagnosticOrder(String visitId, String patientId, String doctorId,
                                                String testType, String priority, String centreId) {
        long count = diagnosticOrderRepository.count();
        String orderId = String.format("DO-%04d", 5001 + count);
        while (diagnosticOrderRepository.existsById(orderId)) {
            count++;
            orderId = String.format("DO-%04d", 5001 + count);
        }

        DiagnosticOrder order = new DiagnosticOrder(
                orderId, visitId, patientId, doctorId, testType, priority, centreId
        );
        return diagnosticOrderRepository.save(order);
    }

    @Transactional
    public DiagnosticOrder scheduleOrder(String orderId, LocalDateTime scheduledDate, String centreId) {
        DiagnosticOrder order = diagnosticOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnostic order not found: " + orderId));

        order.setStatus("SCHEDULED");
        order.setScheduledDate(scheduledDate != null ? scheduledDate : LocalDateTime.now().plusHours(1));
        if (centreId != null) order.setDiagnosticCentreId(centreId);
        return diagnosticOrderRepository.save(order);
    }

    @Transactional
    public DiagnosticOrder startOrder(String orderId) {
        DiagnosticOrder order = diagnosticOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnostic order not found: " + orderId));

        order.setStatus("IN_PROGRESS");
        return diagnosticOrderRepository.save(order);
    }

    @Transactional
    public DiagnosticOrder completeTest(String orderId, String resultsSummary) {
        DiagnosticOrder order = diagnosticOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnostic order not found: " + orderId));

        order.setStatus("COMPLETED");
        order.setCompletedDate(LocalDateTime.now());
        order.setResultsSummary(resultsSummary != null ? resultsSummary : "Diagnostic procedure completed successfully.");
        return diagnosticOrderRepository.save(order);
    }

    @Transactional
    public DiagnosticOrder reviewReport(String orderId, String doctorReviewNotes) {
        return reviewReport(orderId, doctorReviewNotes, null);
    }

    @Transactional
    public DiagnosticOrder reviewReport(String orderId, String doctorReviewNotes, String doctorId) {
        DiagnosticOrder order = diagnosticOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnostic order not found: " + orderId));

        // Keep status COMPLETED - doctor review updates notes and timestamp without confusing status change
        order.setStatus("COMPLETED");
        order.setDoctorReviewNotes(doctorReviewNotes);
        order.setReviewedAt(LocalDateTime.now());
        if (doctorId != null && !doctorId.trim().isEmpty()) {
            order.setReviewedByDoctorId(doctorId);
        } else if (order.getDoctorId() != null) {
            order.setReviewedByDoctorId(order.getDoctorId());
        }
        return diagnosticOrderRepository.save(order);
    }

    @Transactional
    public DiagnosticOrder cancelOrder(String orderId, String reason) {
        DiagnosticOrder order = diagnosticOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnostic order not found: " + orderId));

        order.setStatus("CANCELLED");
        if (reason != null && !reason.trim().isEmpty()) {
            order.setResultsSummary("Cancelled: " + reason);
        }
        return diagnosticOrderRepository.save(order);
    }

    @Transactional
    public DiagnosticCentre addDiagnosticCentre(DiagnosticCentre centre) {
        return diagnosticCentreRepository.save(centre);
    }
}
