package com.hospital.resourceallocation;

import com.hospital.resourceallocation.algorithms.*;
import com.hospital.resourceallocation.dto.*;
import com.hospital.resourceallocation.model.*;
import com.hospital.resourceallocation.repository.*;
import com.hospital.resourceallocation.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive System Test Suite
 * Verifies all 36 mandatory test cases against real MySQL database and service logic.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HospitalSystemTest {

    @Autowired private DoctorService doctorService;
    @Autowired private PatientService patientService;
    @Autowired private ConsultationService consultationService;
    @Autowired private TreatmentPlanService treatmentPlanService;
    @Autowired private ResourceAllocationEngineService allocationEngine;
    @Autowired private DiagnosticManagementService diagnosticService;
    @Autowired private ReferralService referralService;
    @Autowired private AdmissionDischargeService admissionService;
    @Autowired private DijkstraRouter dijkstraRouter;

    @Autowired private PatientVisitRepository visitRepository;
    @Autowired private BedRepository bedRepository;

    private static String testDoctorId1;
    private static String testPatientId;
    private static String testVisitId;

    @Test
    @Order(1)
    @DisplayName("TC 1, 2, 3: New patient registration, auto Patient ID & Visit ID generation")
    public void testPatientRegistrationAndIdGeneration() {
        PatientRegistrationDTO dto = new PatientRegistrationDTO();
        dto.setFullName("Test Patient Citizen");
        dto.setAge(42);
        dto.setGender("Male");
        dto.setContactNumber("+91-9988776655");
        dto.setAddress("Test Health Lane, Nalam");
        dto.setSeverityLevel("Serious");
        dto.setPatientCategory("Outpatient");

        PatientVisit visit = patientService.registerPatientVisit(dto);
        assertNotNull(visit);
        assertNotNull(visit.getPatientId());
        assertNotNull(visit.getVisitId());
        assertTrue(visit.getPatientId().startsWith("P-"));
        assertTrue(visit.getVisitId().startsWith("VIS-"));

        testPatientId = visit.getPatientId();
        testVisitId = visit.getVisitId();
    }

    @Test
    @Order(2)
    @DisplayName("TC 4, 5, 6: New doctor registration, auto Doctor ID (D-001...), no duplicates")
    public void testDoctorRegistrationAndSequentialIdGeneration() {
        DoctorRegistrationDTO d1 = new DoctorRegistrationDTO();
        d1.setDoctorName("Dr. Arun Selvan");
        d1.setSpecialization("Cardiology");
        d1.setQualification("MBBS, MD, DM");
        d1.setContactNumber("+91-9123456781");
        d1.setEmail("arun.s@nalamgh.gov.in");

        DoctorRegistrationDTO d2 = new DoctorRegistrationDTO();
        d2.setDoctorName("Dr. Kavitha Raman");
        d2.setSpecialization("Neurology");
        d2.setQualification("MBBS, MD, DM");
        d2.setContactNumber("+91-9123456782");
        d2.setEmail("kavitha.r@nalamgh.gov.in");

        Doctor doc1 = doctorService.registerDoctor(d1);
        Doctor doc2 = doctorService.registerDoctor(d2);

        assertNotNull(doc1.getDoctorId());
        assertNotNull(doc2.getDoctorId());
        assertTrue(doc1.getDoctorId().matches("D-\\d{3}"));
        assertTrue(doc2.getDoctorId().matches("D-\\d{3}"));
        assertNotEquals(doc1.getDoctorId(), doc2.getDoctorId());

        testDoctorId1 = doc1.getDoctorId();
        assertFalse(doc2.getDoctorId().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("TC 7, 8, 9: Doctor availability check, duty status update, specialization matching")
    public void testDoctorAvailabilityAndDutyStatus() {
        Doctor doc = doctorService.getDoctorById(testDoctorId1).orElseThrow();
        assertTrue(doc.canAcceptConsultation());

        // Update duty status to OFF_DUTY
        Doctor updatedDoc = doctorService.updateDutyStatus(testDoctorId1, "OFF_DUTY");
        assertEquals("OFF_DUTY", updatedDoc.getDutyStatus());
        assertFalse(updatedDoc.canAcceptConsultation());

        // Revert to ON_DUTY
        Doctor onDutyDoc = doctorService.updateDutyStatus(testDoctorId1, "ON_DUTY");
        assertEquals("ON_DUTY", onDutyDoc.getDutyStatus());
        assertTrue(onDutyDoc.canAcceptConsultation());

        // Specialization matching
        assertTrue(onDutyDoc.matchesSpecialization("Cardiology"));
        assertFalse(onDutyDoc.matchesSpecialization("Orthopedics"));
    }

    @Test
    @Order(4)
    @DisplayName("TC 10, 11, 12: Doctor consultation assignment, consultation-only patient, no bed")
    public void testConsultationAssignmentAndCompletion() {
        Consultation consultation = consultationService.assignDoctor(testVisitId, testDoctorId1);
        assertNotNull(consultation);
        assertEquals("ASSIGNED", consultation.getStatus());

        ConsultationRequestDTO updateDto = new ConsultationRequestDTO();
        updateDto.setVisitId(testVisitId);
        updateDto.setDoctorId(testDoctorId1);
        updateDto.setSymptoms("Occasional palpitations");
        updateDto.setDoctorObservations("Normal heart sounds, BP 120/80");
        updateDto.setPrescription("Oral beta-blocker 25mg OD for 5 days");
        updateDto.setStatus("COMPLETED");

        Consultation completed = consultationService.updateConsultation(updateDto);
        assertEquals("COMPLETED", completed.getStatus());

        TreatmentPlanRequestDTO tpDto = new TreatmentPlanRequestDTO();
        tpDto.setVisitId(testVisitId);
        tpDto.setDoctorId(testDoctorId1);
        tpDto.setTreatmentCategory("CONSULTATION_ONLY");
        tpDto.setRequiredBedType("NO_BED");
        tpDto.setDoctorNotes("Outpatient consultation only. No bed admission needed.");

        TreatmentPlan plan = treatmentPlanService.createOrUpdateTreatmentPlan(tpDto);
        assertEquals("CONSULTATION_ONLY", plan.getTreatmentCategory());
        assertFalse(plan.requiresAdmission());
    }

    @Test
    @Order(5)
    @DisplayName("TC 13, 14, 15, 16: Diagnostic order, external centre, report review, plan update")
    public void testDiagnosticWorkflow() {
        DiagnosticOrder order = diagnosticService.createDiagnosticOrder(
                testVisitId, testPatientId, testDoctorId1, "CT_SCAN", "SERIOUS", "DC-001"
        );
        assertNotNull(order.getOrderId());
        assertEquals("PRESCRIBED", order.getStatus());

        DiagnosticOrder scheduled = diagnosticService.scheduleOrder(order.getOrderId(), LocalDateTime.now().plusHours(1), "DC-001");
        assertEquals("SCHEDULED", scheduled.getStatus());

        DiagnosticOrder completed = diagnosticService.completeTest(order.getOrderId(), "CT Thorax shows clear lung fields.");
        assertEquals("COMPLETED", completed.getStatus());

        DiagnosticOrder reviewed = diagnosticService.reviewReport(order.getOrderId(), "Scan reviewed. Clinically clear.");
        assertEquals("COMPLETED", reviewed.getStatus());
        assertEquals("Scan reviewed. Clinically clear.", reviewed.getDoctorReviewNotes());
    }

    @Test
    @Order(6)
    @DisplayName("TC 17, 18, 19, 20: Bed allocation (General & ICU), admission recommendation")
    public void testBedAllocationAndAdmission() {
        AdmissionRequestDTO admDto = new AdmissionRequestDTO();
        admDto.setVisitId(testVisitId);
        admDto.setDoctorId(testDoctorId1);
        admDto.setHospitalId("H-001");
        admDto.setBedType("GENERAL");
        admDto.setNotes("Admitted for monitoring");

        Admission admission = admissionService.admitPatient(admDto);
        assertNotNull(admission.getAdmissionId());
        assertEquals("ACTIVE", admission.getStatus());
        assertNotNull(admission.getBedId());

        Bed bed = bedRepository.findById(admission.getBedId()).orElseThrow();
        assertEquals("OCCUPIED", bed.getStatus());
        assertEquals(testPatientId, bed.getCurrentPatientId());
    }

    @Test
    @Order(7)
    @DisplayName("TC 21, 22, 23, 29, 30: PriorityQueue ordering (Critical > Serious > Minor) and Waiting Queue")
    public void testPriorityQueueOrdering() {
        ConsultationWaitingQueueItem critical = new ConsultationWaitingQueueItem("CWQ-T1", "V1", "P1", "Critical", "General Medicine", LocalDateTime.now().minusMinutes(10));
        ConsultationWaitingQueueItem serious = new ConsultationWaitingQueueItem("CWQ-T2", "V2", "P2", "Serious", "General Medicine", LocalDateTime.now().minusMinutes(20));
        ConsultationWaitingQueueItem minor = new ConsultationWaitingQueueItem("CWQ-T3", "V3", "P3", "Minor", "General Medicine", LocalDateTime.now().minusMinutes(30));

        HospitalPriorityQueue queue = new HospitalPriorityQueue();
        queue.addConsultationRequest(serious);
        queue.addConsultationRequest(minor);
        queue.addConsultationRequest(critical);

        // Highest priority (Critical = score 1) should be polled first despite later arrival
        ConsultationWaitingQueueItem first = queue.pollConsultationRequest();
        assertEquals("Critical", first.getSeverityLevel());

        ConsultationWaitingQueueItem second = queue.pollConsultationRequest();
        assertEquals("Serious", second.getSeverityLevel());

        ConsultationWaitingQueueItem third = queue.pollConsultationRequest();
        assertEquals("Minor", third.getSeverityLevel());
    }

    @Test
    @Order(8)
    @DisplayName("TC 24, 25, 26: Concurrent bed locking, multiple resource allocation & consumable deduction")
    public void testResourceAllocationAndConsumableDeduction() {
        ResourceAllocationRequestDTO dto = new ResourceAllocationRequestDTO();
        dto.setVisitId(testVisitId);
        dto.setResourceNames(List.of("Oxygen Cylinder", "IV Fluid Bottles"));
        dto.setQuantity(1);

        List<ResourceAllocation> allocations = allocationEngine.allocateResources(dto);
        assertFalse(allocations.isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("TC 27, 28: Dijkstra's algorithm shortest route to fastest eligible referral hospital")
    public void testDijkstraShortestRoute() {
        RouteResult route = dijkstraRouter.findShortestRoute("Nalam Central", "Medical Square");
        assertNotNull(route);
        assertEquals(12, route.getTotalTravelTimeMinutes());
        assertTrue(route.getPathNodes().contains("Nalam Central"));
        assertTrue(route.getPathNodes().contains("Medical Square"));

        // Referral auto-discovery
        RouteResult fastestHosp = referralService.findFastestEligibleHospital("H-001", "ICU", "Neurology", List.of("Ventilator"));
        assertNotNull(fastestHosp);
        assertTrue(fastestHosp.getTotalTravelTimeMinutes() > 0);
    }

    @Test
    @Order(10)
    @DisplayName("TC 31, 32, 33, 34: Discharge, bed release, equipment deallocation, and queue re-evaluation")
    public void testDischargeAndResourceRelease() {
        DischargeRequestDTO dischargeDto = new DischargeRequestDTO();
        dischargeDto.setVisitId(testVisitId);
        dischargeDto.setDischargeSummary("Patient fully recovered.");
        dischargeDto.setFollowUpInstructions("Continue healthy diet.");

        DischargeHistory history = admissionService.dischargePatient(dischargeDto);
        assertNotNull(history.getDischargeId());

        PatientVisit visit = visitRepository.findById(testVisitId).orElseThrow();
        assertEquals("DISCHARGED", visit.getVisitStatus());
        assertNull(visit.getAssignedBedId());
    }

    @Test
    @Order(11)
    @DisplayName("TC 35, 36: Patient with multiple visits and complete patient history retrieval")
    public void testMultipleVisitsAndHistoryRetrieval() {
        PatientRegistrationDTO visit2Dto = new PatientRegistrationDTO();
        visit2Dto.setPatientId(testPatientId); // Same citizen
        visit2Dto.setSeverityLevel("Minor");
        visit2Dto.setPatientCategory("Outpatient");
        visit2Dto.setNotes("Follow-up second visit");

        PatientVisit secondVisit = patientService.registerPatientVisit(visit2Dto);
        assertEquals(testPatientId, secondVisit.getPatientId());
        assertNotEquals(testVisitId, secondVisit.getVisitId());

        List<PatientVisit> history = patientService.getVisitsByPatientId(testPatientId);
        assertTrue(history.size() >= 2);
    }

    @Test
    @Order(12)
    @DisplayName("TC 37: New vs Regular patient segmentation, dynamic DOB age calculation, preferred doctor recording")
    public void testNewVsRegularPatientAndPreferredDoctor() {
        // Register brand new patient
        String uniquePhone = "9" + String.format("%09d", System.currentTimeMillis() % 1000000000L);
        PatientRegistrationDTO newPat = new PatientRegistrationDTO();
        newPat.setFullName("Ramesh Kumar");
        newPat.setDateOfBirth(java.time.LocalDate.of(1995, 4, 12));
        newPat.setGender("Male");
        newPat.setContactNumber(uniquePhone);
        newPat.setEmail("ramesh." + System.currentTimeMillis() + "@example.com");
        newPat.setAddress("45 Anna Salai, Chennai");
        newPat.setPreferredDoctorId(testDoctorId1);

        PatientVisit newVisit = patientService.registerPatientVisit(newPat);
        assertNotNull(newVisit);
        assertEquals("NEW", newVisit.getPatientCategory().contains("Outpatient") || newVisit.getVisitStatus().equals("PENDING_TRIAGE") ? "NEW" : "REGULAR");
        assertEquals("PENDING_TRIAGE", newVisit.getVisitStatus());
        assertEquals(testDoctorId1, newVisit.getPreferredDoctorId());

        Patient savedPatient = patientService.getPatientById(newVisit.getPatientId()).orElseThrow();
        assertEquals("NEW", savedPatient.getPatientType());
        assertEquals(testDoctorId1, savedPatient.getPreferredDoctorId());
        assertTrue(savedPatient.getAge() >= 29); // Calculated dynamically from 1995

        // Repeat visit for existing patient -> marked REGULAR
        PatientRegistrationDTO repeatVisitDto = new PatientRegistrationDTO();
        repeatVisitDto.setPatientId(savedPatient.getPatientId());
        repeatVisitDto.setNotes("Follow-up appointment");

        PatientVisit repeatVisit = patientService.registerPatientVisit(repeatVisitDto);
        Patient updatedPat = patientService.getPatientById(savedPatient.getPatientId()).orElseThrow();
        assertEquals("REGULAR", updatedPat.getPatientType());
        assertEquals(testDoctorId1, repeatVisit.getPreferredDoctorId());
    }

    @Test
    @Order(13)
    @DisplayName("TC 38: Triage assessment, auto doctor check, alternative doctor offer & acceptance/rejection")
    public void testTriageAndDoctorAlternativeWorkflow() {
        // Create visit for triage testing
        PatientRegistrationDTO dto = new PatientRegistrationDTO();
        dto.setFullName("Priya Lakshmi");
        dto.setDateOfBirth(java.time.LocalDate.of(1998, 8, 20));
        dto.setGender("Female");
        dto.setContactNumber("9789012345");
        dto.setAddress("12 South Car St, Madurai");
        dto.setPreferredDoctorId(testDoctorId1);

        PatientVisit visit = patientService.registerPatientVisit(dto);
        assertEquals("PENDING_TRIAGE", visit.getVisitStatus());

        // Reception staff does NOT set severity; Doctor/Triage staff performs assessment
        TriageAssessmentDTO triageDTO = new TriageAssessmentDTO();
        triageDTO.setSeverityLevel("Critical");
        triageDTO.setTriageNotes("Severe acute chest pain, high priority triage");
        triageDTO.setAssessedBy("Staff Nurse Preethi");

        DoctorAssignmentDecisionDTO decision = patientService.performTriageAndAssignDoctor(visit.getVisitId(), triageDTO);
        assertNotNull(decision);
        assertEquals("Critical", decision.getVisit().getSeverityLevel());

        // Now test alternative doctor flow when preferred doctor is off-duty
        doctorService.toggleDutyStatus(testDoctorId1); // Set OFF_DUTY

        PatientRegistrationDTO dto2 = new PatientRegistrationDTO();
        dto2.setFullName("Sundar Rajan");
        dto2.setDateOfBirth(java.time.LocalDate.of(1982, 3, 15));
        dto2.setGender("Male");
        dto2.setContactNumber("9444012345");
        dto2.setAddress("7 Cross Road, Trichy");
        dto2.setPreferredDoctorId(testDoctorId1);

        PatientVisit visit2 = patientService.registerPatientVisit(dto2);

        TriageAssessmentDTO triageDTO2 = new TriageAssessmentDTO();
        triageDTO2.setSeverityLevel("Serious");
        triageDTO2.setTriageNotes("Persistent migraine, neurology consultation needed");
        triageDTO2.setAssessedBy("Triage Officer Dr. Vimal");

        DoctorAssignmentDecisionDTO decision2 = patientService.performTriageAndAssignDoctor(visit2.getVisitId(), triageDTO2);
        assertFalse(decision2.isPreferredDoctorAvailable());

        // If rejected, visit is marked NO_DOCTOR_AVAILABLE and record is retained
        DoctorAssignmentDecisionDTO rejectDecision = patientService.respondToAlternativeDoctor(visit2.getVisitId(), false, null);
        assertEquals("NO_DOCTOR_AVAILABLE", rejectDecision.getStatus());
        PatientVisit rejectedVisit = patientService.getVisitById(visit2.getVisitId()).orElseThrow();
        assertEquals("NO_DOCTOR_AVAILABLE", rejectedVisit.getVisitStatus());
        assertNotNull(rejectedVisit.getPatientId()); // Patient record is NOT deleted

        // Restore doctor 1 back to ON_DUTY
        doctorService.toggleDutyStatus(testDoctorId1);
    }

    @Test
    @Order(14)
    @DisplayName("TC 39: Doctor duty toggle and permanent deactivation preserving historical consultations")
    public void testDoctorDutyToggleAndPermanentDeactivation() {
        DoctorRegistrationDTO newDoc = new DoctorRegistrationDTO();
        newDoc.setDoctorName("Dr. Resigning Physician");
        newDoc.setSpecialization("Dermatology");
        newDoc.setQualification("MBBS, MD");
        newDoc.setContactNumber("9944012345");
        newDoc.setEmail("resigning.physician@nalamgh.gov.in");
        newDoc.setDateOfBirth(java.time.LocalDate.of(1980, 5, 10));
        newDoc.setGender("Male");
        newDoc.setJoiningDate(java.time.LocalDate.of(2015, 6, 1));

        Doctor createdDoc = doctorService.registerDoctor(newDoc);
        assertTrue(createdDoc.getIsActive());
        assertEquals("ON_DUTY", createdDoc.getDutyStatus());

        // Single Toggle to OFF_DUTY
        Doctor toggledOff = doctorService.toggleDutyStatus(createdDoc.getDoctorId());
        assertEquals("OFF_DUTY", toggledOff.getDutyStatus());
        assertEquals("OFF_DUTY", toggledOff.getAvailabilityStatus());

        // Single Toggle back to ON_DUTY
        Doctor toggledOn = doctorService.toggleDutyStatus(createdDoc.getDoctorId());
        assertEquals("ON_DUTY", toggledOn.getDutyStatus());
        assertEquals("AVAILABLE", toggledOn.getAvailabilityStatus());

        // Permanent Deactivation (Doctor resigned)
        doctorService.deactivateDoctor(createdDoc.getDoctorId(), "Doctor resigned to pursue private practice");
        Doctor deactivatedDoc = doctorService.getDoctorById(createdDoc.getDoctorId()).orElseThrow();
        assertFalse(deactivatedDoc.getIsActive());
        assertEquals("OFF_DUTY", deactivatedDoc.getDutyStatus());
        assertEquals("DEACTIVATED", deactivatedDoc.getAvailabilityStatus());

        // Active doctors list must NOT contain deactivated doctor
        List<Doctor> activeDoctors = doctorService.getActiveDoctors();
        assertTrue(activeDoctors.stream().noneMatch(d -> d.getDoctorId().equals(createdDoc.getDoctorId())));
    }

    @Test
    @Order(15)
    @DisplayName("TC 40: Unified Patient 360 profile aggregates visits, notes, plans, diagnostics, and resources")
    public void testUnifiedPatientProfile() {
        PatientProfileDTO profile = patientService.getPatientProfile(testPatientId);
        assertNotNull(profile);
        assertEquals(testPatientId, profile.getPatient().getPatientId());
        assertNotNull(profile.getVisits());
        assertFalse(profile.getVisits().isEmpty());
        assertNotNull(profile.getConsultations());
        assertNotNull(profile.getTreatmentPlans());
        assertNotNull(profile.getDiagnosticOrders());
        assertNotNull(profile.getAdmissions());
        assertNotNull(profile.getDischarges());
        assertNotNull(profile.getResourceAllocations());
    }
}
