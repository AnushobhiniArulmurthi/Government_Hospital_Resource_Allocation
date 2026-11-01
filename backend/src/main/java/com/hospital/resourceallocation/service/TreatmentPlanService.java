package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.algorithms.RouteResult;
import com.hospital.resourceallocation.dto.TreatmentPlanRequestDTO;
import com.hospital.resourceallocation.model.*;
import com.hospital.resourceallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Doctor-Based Treatment Plan Service
 * Supports 7 clinical treatment pathways, flexible resource requirements,
 * automatic bed and equipment allocation, referral recommendations, and queue placement.
 */
@Service
public class TreatmentPlanService {

    private final TreatmentPlanRepository treatmentPlanRepository;
    private final ConsultationRepository consultationRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final BedRepository bedRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final ResourceHistoryRepository historyRepository;
    private final AdmissionRepository admissionRepository;
    private final ReferralService referralService;
    private final WaitingQueueService waitingQueueService;

    @Autowired
    public TreatmentPlanService(TreatmentPlanRepository treatmentPlanRepository,
                                ConsultationRepository consultationRepository,
                                PatientVisitRepository patientVisitRepository,
                                DiagnosticOrderRepository diagnosticOrderRepository,
                                BedRepository bedRepository,
                                ResourceRepository resourceRepository,
                                ResourceAllocationRepository allocationRepository,
                                ResourceHistoryRepository historyRepository,
                                AdmissionRepository admissionRepository,
                                ReferralService referralService,
                                WaitingQueueService waitingQueueService) {
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.consultationRepository = consultationRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.bedRepository = bedRepository;
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.historyRepository = historyRepository;
        this.admissionRepository = admissionRepository;
        this.referralService = referralService;
        this.waitingQueueService = waitingQueueService;
    }

    @Transactional
    public TreatmentPlan createOrUpdateTreatmentPlan(TreatmentPlanRequestDTO dto) {
        PatientVisit visit = patientVisitRepository.findById(dto.getVisitId())
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + dto.getVisitId()));

        Consultation consultation = consultationRepository.findByVisitId(dto.getVisitId()).orElse(null);
        String consultationId = consultation != null ? consultation.getConsultationId() : "CON-AUTO";
        String doctorId = dto.getDoctorId() != null ? dto.getDoctorId() : (visit.getAssignedDoctorId() != null ? visit.getAssignedDoctorId() : "D-001");

        TreatmentPlan plan = treatmentPlanRepository.findByVisitId(dto.getVisitId())
                .orElseGet(() -> {
                    long count = treatmentPlanRepository.count();
                    String planId = String.format("TP-%04d", 4001 + count);
                    while (treatmentPlanRepository.existsById(planId)) {
                        count++;
                        planId = String.format("TP-%04d", 4001 + count);
                    }
                    TreatmentPlan newPlan = new TreatmentPlan();
                    newPlan.setTreatmentPlanId(planId);
                    newPlan.setVisitId(dto.getVisitId());
                    newPlan.setPatientId(visit.getPatientId());
                    newPlan.setConsultationId(consultationId);
                    newPlan.setDoctorId(doctorId);
                    return newPlan;
                });

        plan.setTreatmentCategory(dto.getTreatmentCategory());
        plan.setDoctorNotes(dto.getDoctorNotes());
        plan.setRequiredBedType(dto.getRequiredBedType() != null ? dto.getRequiredBedType() : "NO_BED");
        plan.setRequiredDoctorSpecialization(dto.getRequiredDoctorSpecialization());

        if (dto.getRequiredResources() != null && !dto.getRequiredResources().isEmpty()) {
            plan.setRequiredResources(String.join(", ", dto.getRequiredResources()));
        }

        // Process diagnostic test orders if specified
        if (dto.getDiagnosticTests() != null && !dto.getDiagnosticTests().isEmpty()) {
            plan.setDiagnosticOrdersSummary(String.join(", ", dto.getDiagnosticTests()));
            for (String testType : dto.getDiagnosticTests()) {
                long orderCount = diagnosticOrderRepository.count();
                String orderId = String.format("DO-%04d", 5001 + orderCount);
                while (diagnosticOrderRepository.existsById(orderId)) {
                    orderCount++;
                    orderId = String.format("DO-%04d", 5001 + orderCount);
                }
                DiagnosticOrder order = new DiagnosticOrder(
                        orderId, dto.getVisitId(), visit.getPatientId(), doctorId,
                        testType, visit.getSeverityLevel(), dto.getDiagnosticCentreId()
                );
                diagnosticOrderRepository.save(order);
            }
        }

        plan.setStatus("CREATED");

        // Automatic Bed and Resource Allocation Workflow according to Treatment Plan
        boolean bedRequired = plan.getRequiredBedType() != null && !"NO_BED".equalsIgnoreCase(plan.getRequiredBedType());
        if (bedRequired && visit.getAssignedBedId() == null) {
            List<Bed> availableBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus("H-001", plan.getRequiredBedType(), "AVAILABLE");
            if (!availableBeds.isEmpty()) {
                Bed allocatedBed = availableBeds.get(0);
                allocatedBed.allocate(visit.getPatientId(), dto.getVisitId());
                bedRepository.save(allocatedBed);

                visit.setAssignedBedId(allocatedBed.getBedId());
                visit.setVisitStatus("ADMITTED");
                visit.setPatientCategory("Inpatient");

                long admCount = admissionRepository.count();
                String admissionId = String.format("ADM-%04d", 6001 + admCount);
                while (admissionRepository.existsById(admissionId)) {
                    admCount++;
                    admissionId = String.format("ADM-%04d", 6001 + admCount);
                }
                Admission admission = new Admission(
                        admissionId, dto.getVisitId(), visit.getPatientId(), doctorId,
                        "H-001", allocatedBed.getBedId(), "Auto-admitted based on Doctor Treatment Plan (" + plan.getTreatmentCategory() + ")"
                );
                admissionRepository.save(admission);
            } else {
                // Bed unavailable at primary hospital: check eligible referral hospitals via Dijkstra
                RouteResult routeResult = referralService.findFastestEligibleHospital(
                        "H-001", plan.getRequiredBedType(), plan.getRequiredDoctorSpecialization(), dto.getRequiredResources()
                );
                if (routeResult != null) {
                    visit.setVisitStatus("REFERRAL_RECOMMENDED");
                    plan.setDoctorNotes((plan.getDoctorNotes() != null ? plan.getDoctorNotes() + " | " : "") +
                            "Referral Recommended: " + routeResult.getDestination() + " (Travel: " + routeResult.getTotalTravelTimeMinutes() + " mins)");
                } else {
                    // No eligible referral hospital: auto-place in resource waiting queue
                    waitingQueueService.enqueueResourceRequest(
                            dto.getVisitId(), visit.getPatientId(), visit.getSeverityLevel(),
                            plan.getRequiredBedType(), plan.getRequiredDoctorSpecialization(),
                            dto.getRequiredResources() != null ? String.join(", ", dto.getRequiredResources()) : "Bed (" + plan.getRequiredBedType() + ")"
                    );
                    visit.setVisitStatus("WAITING_FOR_RESOURCE");
                }
            }
        } else if ("REFERRAL_REQUIRED".equalsIgnoreCase(dto.getTreatmentCategory())) {
            RouteResult routeResult = referralService.findFastestEligibleHospital(
                    "H-001", plan.getRequiredBedType(), plan.getRequiredDoctorSpecialization(), dto.getRequiredResources()
            );
            if (routeResult != null) {
                visit.setVisitStatus("REFERRAL_RECOMMENDED");
                plan.setDoctorNotes((plan.getDoctorNotes() != null ? plan.getDoctorNotes() + " | " : "") +
                        "Referral Recommended: " + routeResult.getDestination() + " (Travel: " + routeResult.getTotalTravelTimeMinutes() + " mins)");
            } else {
                waitingQueueService.enqueueResourceRequest(
                        dto.getVisitId(), visit.getPatientId(), visit.getSeverityLevel(),
                        plan.getRequiredBedType(), plan.getRequiredDoctorSpecialization(),
                        dto.getRequiredResources() != null ? String.join(", ", dto.getRequiredResources()) : "All"
                );
                visit.setVisitStatus("WAITING_FOR_RESOURCE");
            }
        } else if ("DIAGNOSTIC_TESTS_REQUIRED".equalsIgnoreCase(dto.getTreatmentCategory())) {
            visit.setVisitStatus("TREATMENT_PLANNED");
            visit.setPatientCategory("Diagnostic-Based");
        } else if (visit.getVisitStatus() == null || visit.getVisitStatus().startsWith("IN_") || visit.getVisitStatus().equals("PENDING_TRIAGE")) {
            visit.setVisitStatus("TREATMENT_PLANNED");
        }

        // Automatic equipment / resource allocation according to treatment plan
        if (dto.getRequiredResources() != null && !dto.getRequiredResources().isEmpty()) {
            for (String resName : dto.getRequiredResources()) {
                String cleanName = resName.trim();
                if (cleanName.isEmpty() || cleanName.equalsIgnoreCase("None")) continue;
                resourceRepository.findByHospitalIdAndResourceNameIgnoreCase("H-001", cleanName).ifPresent(res -> {
                    if (res.checkAvailability(1)) {
                        res.allocateUnits(1);
                        resourceRepository.save(res);

                        long allocCount = allocationRepository.count();
                        String allocId = String.format("ALL-%04d", 7001 + allocCount);
                        while (allocationRepository.existsById(allocId)) {
                            allocCount++;
                            allocId = String.format("ALL-%04d", 7001 + allocCount);
                        }
                        ResourceAllocation alloc = new ResourceAllocation(
                                allocId, dto.getVisitId(), visit.getPatientId(), res.getResourceId(),
                                res.getResourceName(), res.getCategory(), 1
                        );
                        allocationRepository.save(alloc);

                        historyRepository.save(new ResourceHistory(
                                res.getResourceId(), res.getResourceName(), "ALLOCATED", 1,
                                visit.getPatientId(), dto.getVisitId(), "Auto-allocated per Doctor Treatment Plan"
                        ));
                    }
                });
            }
        }

        patientVisitRepository.save(visit);
        return treatmentPlanRepository.save(plan);
    }

    public List<TreatmentPlan> getAllTreatmentPlans() {
        return treatmentPlanRepository.findAll();
    }

    public Optional<TreatmentPlan> getTreatmentPlanByVisitId(String visitId) {
        return treatmentPlanRepository.findByVisitId(visitId);
    }
}
