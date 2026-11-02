package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.dto.DoctorAssignmentDecisionDTO;
import com.hospital.resourceallocation.dto.PatientProfileDTO;
import com.hospital.resourceallocation.dto.PatientRegistrationDTO;
import com.hospital.resourceallocation.dto.TriageAssessmentDTO;
import com.hospital.resourceallocation.model.ConsultationWaitingQueueItem;
import com.hospital.resourceallocation.model.Doctor;
import com.hospital.resourceallocation.model.Patient;
import com.hospital.resourceallocation.model.PatientVisit;
import com.hospital.resourceallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Patient Management Service
 * Implements:
 * - Patient segmentation into Regular Patients and New Patients
 * - Recording of previously preferred doctors for regular patients
 * - Recording of recommended/selected doctors for new patients
 * - Dynamic age calculation from Date of Birth
 * - Triage assessment (reception staff cannot set severity; medical staff sets Critical -> Serious -> Minor)
 * - Automatic preferred doctor availability check
 * - Alternative doctor offering upon unavailability
 * - Graceful NO_DOCTOR_AVAILABLE status without deleting records
 * - Unified Patient 360 profile aggregation
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationQueueRepository consultationQueueRepository;
    private final ConsultationRepository consultationRepository;
    private final TreatmentPlanRepository treatmentPlanRepository;
    private final DiagnosticOrderRepository diagnosticOrderRepository;
    private final AdmissionRepository admissionRepository;
    private final DischargeHistoryRepository dischargeHistoryRepository;
    private final ReferralRepository referralRepository;
    private final ResourceAllocationRepository resourceAllocationRepository;
    private final ConsultationService consultationService;
    private final WaitingQueueService waitingQueueService;
    private final BedRepository bedRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                          PatientVisitRepository patientVisitRepository,
                          DoctorRepository doctorRepository,
                          ConsultationQueueRepository consultationQueueRepository,
                          ConsultationRepository consultationRepository,
                          TreatmentPlanRepository treatmentPlanRepository,
                          DiagnosticOrderRepository diagnosticOrderRepository,
                          AdmissionRepository admissionRepository,
                          DischargeHistoryRepository dischargeHistoryRepository,
                          ReferralRepository referralRepository,
                          ResourceAllocationRepository resourceAllocationRepository,
                          ConsultationService consultationService,
                          WaitingQueueService waitingQueueService,
                          BedRepository bedRepository) {
        this.patientRepository = patientRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.doctorRepository = doctorRepository;
        this.consultationQueueRepository = consultationQueueRepository;
        this.consultationRepository = consultationRepository;
        this.treatmentPlanRepository = treatmentPlanRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.admissionRepository = admissionRepository;
        this.dischargeHistoryRepository = dischargeHistoryRepository;
        this.referralRepository = referralRepository;
        this.resourceAllocationRepository = resourceAllocationRepository;
        this.consultationService = consultationService;
        this.waitingQueueService = waitingQueueService;
        this.bedRepository = bedRepository;
    }

    /**
     * Intake registration: separates Regular Patients from New Patients,
     * stores Date of Birth and calculates age dynamically,
     * records preferred or recommended doctor.
     * Severity is initialized to PENDING_TRIAGE (reception staff cannot determine severity).
     */
    @Transactional
    public PatientVisit registerPatientVisit(PatientRegistrationDTO dto) {
        dto.validateBusinessRules();

        String patientId = dto.getPatientId();
        Patient patient;

        // Check if existing regular patient by ID or mobile number
        if (patientId != null && !patientId.trim().isEmpty() && patientRepository.existsById(patientId.trim())) {
            patient = patientRepository.findById(patientId.trim()).get();
            patient.setPatientType("REGULAR");
            if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
                patient.setFullName(dto.getFullName().trim());
            }
            if (dto.getDateOfBirth() != null) {
                patient.setDateOfBirth(dto.getDateOfBirth());
            }
            if (dto.getGender() != null) patient.setGender(dto.getGender());
            if (dto.getContactNumber() != null) patient.setContactNumber(dto.getContactNumber());
            if (dto.getEmail() != null) patient.setEmail(dto.getEmail());
            if (dto.getAddress() != null) patient.setAddress(dto.getAddress());
            if (dto.getPreferredDoctorId() != null && !dto.getPreferredDoctorId().trim().isEmpty()) {
                patient.setPreferredDoctorId(dto.getPreferredDoctorId());
            }
            patient = patientRepository.save(patient);
        } else {
            // Check by contact number to see if returning citizen
            List<Patient> existingByPhone = patientRepository.findByContactNumber(dto.getContactNumber());
            if (!existingByPhone.isEmpty()) {
                patient = existingByPhone.get(0);
                patient.setPatientType("REGULAR");
                if (dto.getPreferredDoctorId() != null && !dto.getPreferredDoctorId().trim().isEmpty()) {
                    patient.setPreferredDoctorId(dto.getPreferredDoctorId());
                }
                patient = patientRepository.save(patient);
            } else {
                // Register Brand New Patient
                long count = patientRepository.countTotalPatients();
                patientId = String.format("P-%04d", 1001 + count);
                while (patientRepository.existsById(patientId)) {
                    count++;
                    patientId = String.format("P-%04d", 1001 + count);
                }

                LocalDate dob = dto.getDateOfBirth() != null ? dto.getDateOfBirth() : LocalDate.of(1990, 1, 1);
                patient = new Patient(
                        patientId, dto.getFullName().trim(), dob, dto.getGender(),
                        dto.getContactNumber().trim(), dto.getEmail(), dto.getAddress().trim(),
                        "NEW", dto.getPreferredDoctorId()
                );
                patient = patientRepository.save(patient);
            }
        }

        // Generate next unique Visit ID
        long visitCount = patientVisitRepository.count();
        String visitId = String.format("VIS-%04d", 2001 + visitCount);
        while (patientVisitRepository.existsById(visitId)) {
            visitCount++;
            visitId = String.format("VIS-%04d", 2001 + visitCount);
        }

        String category = dto.getPatientCategory() != null ? dto.getPatientCategory() : "Outpatient";
        String notes = dto.getNotes() != null && !dto.getNotes().trim().isEmpty() ? dto.getNotes().trim() : "Patient arrived at reception intake.";
        String severity = (dto.getSeverityLevel() != null && !dto.getSeverityLevel().trim().isEmpty() && !dto.getSeverityLevel().equalsIgnoreCase("PENDING_TRIAGE")) ?
                dto.getSeverityLevel().trim() : "Normal";

        PatientVisit visit = new PatientVisit(
                visitId, patient.getPatientId(), severity, category, LocalDateTime.now(), notes
        );
        visit.setVisitStatus("REGISTERED");

        // Record preferred doctor for this visit
        String prefDocId = dto.getPreferredDoctorId();
        if (prefDocId == null || prefDocId.trim().isEmpty()) {
            prefDocId = patient.getPreferredDoctorId();
        }
        visit.setPreferredDoctorId(prefDocId);

        return patientVisitRepository.save(visit);
    }

    /**
     * Medical Staff Triage Assessment:
     * Assigns severity (Critical, Serious, Minor), then automatically checks
     * preferred/recommended doctor availability.
     */
    @Transactional
    public DoctorAssignmentDecisionDTO performTriageAndAssignDoctor(String visitId, TriageAssessmentDTO triageDTO) {
        PatientVisit visit = patientVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + visitId));

        String severity = triageDTO.getSeverityLevel();
        if (severity == null || (!severity.equalsIgnoreCase("Critical") && !severity.equalsIgnoreCase("Serious") && !severity.equalsIgnoreCase("Minor"))) {
            throw new IllegalArgumentException("Invalid clinical severity: " + severity + ". Must be Critical, Serious, or Minor.");
        }

        // Capitalize standard format
        if (severity.equalsIgnoreCase("Critical")) severity = "Critical";
        else if (severity.equalsIgnoreCase("Serious")) severity = "Serious";
        else severity = "Minor";

        visit.setSeverityLevel(severity);
        visit.setTriageNotes(triageDTO.getTriageNotes());
        visit.setTriageAssessedBy(triageDTO.getAssessedBy() != null ? triageDTO.getAssessedBy() : "Triage Officer");
        visit.setTriageAssessedTime(LocalDateTime.now());
        visit.setVisitStatus("TRIAGED");

        patientVisitRepository.save(visit);

        // Run automatic doctor availability check
        return checkAndAssignDoctor(visit);
    }

    /**
     * Checks doctor availability:
     * 1. If preferred/recommended doctor is available and on duty -> assign automatically!
     * 2. If unavailable -> query alternative doctors with same specialty and offer choice.
     * 3. If no doctor available -> mark visit NO_DOCTOR_AVAILABLE and retain record.
     */
    @Transactional
    public DoctorAssignmentDecisionDTO checkAndAssignDoctor(PatientVisit visit) {
        DoctorAssignmentDecisionDTO decision = new DoctorAssignmentDecisionDTO();
        decision.setVisitId(visit.getVisitId());
        decision.setPatientId(visit.getPatientId());
        decision.setVisit(visit);

        String prefDocId = visit.getPreferredDoctorId();
        Doctor preferredDoctor = null;
        if (prefDocId != null && !prefDocId.trim().isEmpty()) {
            preferredDoctor = doctorRepository.findById(prefDocId).orElse(null);
        }
        decision.setPreferredDoctor(preferredDoctor);

        String requiredSpec = preferredDoctor != null ? preferredDoctor.getSpecialization() : "General Medicine";

        // Check if preferred doctor is active, on duty, and available
        if (preferredDoctor != null && Boolean.TRUE.equals(preferredDoctor.getIsActive())
                && "ON_DUTY".equalsIgnoreCase(preferredDoctor.getDutyStatus())
                && "AVAILABLE".equalsIgnoreCase(preferredDoctor.getAvailabilityStatus())) {

            // Automatically assign preferred doctor!
            consultationService.assignDoctor(visit.getVisitId(), preferredDoctor.getDoctorId());
            visit.setVisitStatus("IN_CONSULTATION");
            visit.setAssignedDoctorId(preferredDoctor.getDoctorId());
            patientVisitRepository.save(visit);

            decision.setStatus("ASSIGNED");
            decision.setPreferredDoctorAvailable(true);
            decision.setAssignedDoctor(preferredDoctor);
            decision.setMessage("Preferred doctor Dr. " + preferredDoctor.getDoctorName() + " is available and has been automatically assigned.");
            return decision;
        }

        // Preferred doctor is unavailable (or no preferred doctor was selected)
        decision.setPreferredDoctorAvailable(false);

        // Find alternative suitable available doctors
        final Doctor finalPreferredDoctor = preferredDoctor;
        final String finalRequiredSpec = requiredSpec;
        List<Doctor> alternativeDoctors = doctorRepository.findByIsActiveTrueAndHospitalIdAndDutyStatusAndAvailabilityStatus("H-001", "ON_DUTY", "AVAILABLE")
                .stream()
                .filter(d -> d.matchesSpecialization(finalRequiredSpec) && (finalPreferredDoctor == null || !d.getDoctorId().equals(finalPreferredDoctor.getDoctorId())))
                .toList();

        decision.setAlternativeDoctors(alternativeDoctors);

        if (!alternativeDoctors.isEmpty()) {
            visit.setAlternativeDoctorOffered(true);
            visit.setVisitStatus("AWAITING_ALTERNATIVE_CONFIRMATION");
            patientVisitRepository.save(visit);

            decision.setStatus("ALTERNATIVE_OFFERED");
            String prefName = preferredDoctor != null ? "Dr. " + preferredDoctor.getDoctorName() : "Selected doctor";
            decision.setMessage(prefName + " is currently off-duty or in consultation. " + alternativeDoctors.size() + " alternative suitable doctor(s) are available.");
            return decision;
        } else {
            // No suitable doctor available at all right now
            visit.setVisitStatus("NO_DOCTOR_AVAILABLE");
            patientVisitRepository.save(visit);

            // Enqueue into consultation waiting queue with assigned clinical severity
            enqueueIntoConsultationQueue(visit, requiredSpec);

            decision.setStatus("NO_DOCTOR_AVAILABLE");
            decision.setMessage("No suitable doctor currently available on duty. Visit has been marked as 'No Doctor Available' and added to priority waiting queue.");
            return decision;
        }
    }

    /**
     * Handles patient acceptance or rejection of an alternative doctor.
     */
    @Transactional
    public DoctorAssignmentDecisionDTO respondToAlternativeDoctor(String visitId, boolean accepted, String selectedDoctorId) {
        PatientVisit visit = patientVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + visitId));

        DoctorAssignmentDecisionDTO decision = new DoctorAssignmentDecisionDTO();
        decision.setVisitId(visitId);
        decision.setPatientId(visit.getPatientId());
        decision.setVisit(visit);

        if (accepted && selectedDoctorId != null && !selectedDoctorId.trim().isEmpty()) {
            Doctor selectedDoc = doctorRepository.findById(selectedDoctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + selectedDoctorId));

            if (!selectedDoc.canAcceptConsultation()) {
                throw new IllegalStateException("Selected doctor Dr. " + selectedDoc.getDoctorName() + " is no longer available.");
            }

            consultationService.assignDoctor(visitId, selectedDoctorId);
            visit.setAssignedDoctorId(selectedDoctorId);
            visit.setAlternativeDoctorAccepted(true);
            visit.setVisitStatus("IN_CONSULTATION");
            patientVisitRepository.save(visit);

            decision.setStatus("ASSIGNED");
            decision.setAssignedDoctor(selectedDoc);
            decision.setMessage("Alternative doctor Dr. " + selectedDoc.getDoctorName() + " confirmed and assigned for consultation.");
            return decision;
        } else {
            // Patient rejected alternative doctor
            visit.setAlternativeDoctorAccepted(false);
            visit.setVisitStatus("NO_DOCTOR_AVAILABLE");
            patientVisitRepository.save(visit);

            String prefDocId = visit.getPreferredDoctorId();
            Doctor prefDoc = prefDocId != null ? doctorRepository.findById(prefDocId).orElse(null) : null;
            String spec = prefDoc != null ? prefDoc.getSpecialization() : "General Medicine";

            // Enqueue into priority queue for when preferred doctor becomes available
            enqueueIntoConsultationQueue(visit, spec);

            decision.setStatus("NO_DOCTOR_AVAILABLE");
            decision.setMessage("Patient declined alternative doctor. Visit marked as 'No Doctor Available' and retained in episode history.");
            return decision;
        }
    }

    private void enqueueIntoConsultationQueue(PatientVisit visit, String requiredSpec) {
        long queueCount = consultationQueueRepository.count();
        String queueId = String.format("CWQ-%04d", 9001 + queueCount);
        while (consultationQueueRepository.existsById(queueId)) {
            queueCount++;
            queueId = String.format("CWQ-%04d", 9001 + queueCount);
        }

        ConsultationWaitingQueueItem queueItem = new ConsultationWaitingQueueItem(
                queueId, visit.getVisitId(), visit.getPatientId(), visit.getSeverityLevel(), requiredSpec, LocalDateTime.now()
        );
        consultationQueueRepository.save(queueItem);
        waitingQueueService.getConsultationWaitingQueue(); // sync
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> searchPatients(String search) {
        if (search == null || search.trim().isEmpty()) {
            return patientRepository.findAll();
        }
        String q = search.trim();
        if (patientRepository.existsById(q)) {
            return List.of(patientRepository.findById(q).get());
        }
        List<Patient> byName = patientRepository.findByFullNameContainingIgnoreCase(q);
        if (!byName.isEmpty()) return byName;
        return patientRepository.findByContactNumber(q);
    }

    public List<com.hospital.resourceallocation.dto.PatientOverviewDTO> getPatientOverviews(String search) {
        List<Patient> patients = searchPatients(search);
        List<com.hospital.resourceallocation.dto.PatientOverviewDTO> list = new java.util.ArrayList<>();

        for (Patient p : patients) {
            com.hospital.resourceallocation.dto.PatientOverviewDTO dto = new com.hospital.resourceallocation.dto.PatientOverviewDTO();
            dto.setPatientId(p.getPatientId());
            dto.setFullName(p.getFullName());
            dto.setDateOfBirth(p.getDateOfBirth());
            dto.setAge(p.getAge());
            dto.setGender(p.getGender());
            dto.setContactNumber(p.getContactNumber());
            dto.setEmail(p.getEmail());
            dto.setAddress(p.getAddress());
            dto.setPatientType(p.getPatientType());

            List<PatientVisit> visits = patientVisitRepository.findByPatientIdOrderByArrivalTimeDesc(p.getPatientId());
            if (!visits.isEmpty()) {
                PatientVisit latest = visits.get(0);
                dto.setVisitId(latest.getVisitId());
                dto.setMedicalCondition(latest.getNotes());
                dto.setSeverity(latest.getSeverityLevel() != null && !latest.getSeverityLevel().equalsIgnoreCase("PENDING_TRIAGE") ?
                        latest.getSeverityLevel() : "Normal");
                dto.setVisitStatus(latest.getVisitStatus());
                dto.setRegistrationTime(latest.getArrivalTime());

                if (latest.getAssignedBedId() != null) {
                    bedRepository.findById(latest.getAssignedBedId()).ifPresent(b -> dto.setAssignedBed(b.getBedNumber() + " (" + b.getBedType() + ")"));
                }
            } else {
                dto.setSeverity("Normal");
                dto.setMedicalCondition("No prior clinical episodes recorded");
                dto.setVisitStatus("REGISTERED");
            }
            list.add(dto);
        }
        return list;
    }

    public Optional<Patient> getPatientById(String patientId) {
        return patientRepository.findById(patientId);
    }

    public List<PatientVisit> getAllVisits() {
        return patientVisitRepository.findAll();
    }

    public List<PatientVisit> getVisitsByPatientId(String patientId) {
        return patientVisitRepository.findByPatientIdOrderByArrivalTimeDesc(patientId);
    }

    public Optional<PatientVisit> getVisitById(String visitId) {
        return patientVisitRepository.findById(visitId);
    }

    /**
     * Unified Patient 360 Record aggregation:
     * Combines demographics, preferred doctor, visit histories, consultations,
     * treatment plans, diagnostic reports & doctor reviews, admissions, referrals,
     * and resource allocations.
     */
    public PatientProfileDTO getPatientProfile(String patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        PatientProfileDTO profile = new PatientProfileDTO();
        profile.setPatient(patient);

        if (patient.getPreferredDoctorId() != null) {
            doctorRepository.findById(patient.getPreferredDoctorId()).ifPresent(profile::setPreferredDoctor);
        }

        profile.setVisits(patientVisitRepository.findByPatientIdOrderByArrivalTimeDesc(patientId));
        profile.setConsultations(consultationRepository.findByPatientId(patientId));
        profile.setTreatmentPlans(treatmentPlanRepository.findByPatientId(patientId));
        profile.setDiagnosticOrders(diagnosticOrderRepository.findByPatientId(patientId));
        profile.setAdmissions(admissionRepository.findByPatientId(patientId));
        profile.setDischargeHistories(dischargeHistoryRepository.findByPatientIdOrderByDischargeDateDesc(patientId));
        profile.setReferrals(referralRepository.findByPatientId(patientId));
        profile.setResourceAllocations(resourceAllocationRepository.findByPatientId(patientId));

        return profile;
    }
}
