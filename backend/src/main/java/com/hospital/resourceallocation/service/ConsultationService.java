package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.dto.ConsultationRequestDTO;
import com.hospital.resourceallocation.model.Consultation;
import com.hospital.resourceallocation.model.Doctor;
import com.hospital.resourceallocation.model.PatientVisit;
import com.hospital.resourceallocation.repository.ConsultationQueueRepository;
import com.hospital.resourceallocation.repository.ConsultationRepository;
import com.hospital.resourceallocation.repository.DoctorRepository;
import com.hospital.resourceallocation.repository.PatientVisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Doctor Consultation Service
 * Coordinates doctor examination records, symptoms, notes, prescriptions, and status transitions.
 */
@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationQueueRepository consultationQueueRepository;
    private final WaitingQueueService waitingQueueService;

    @Autowired
    public ConsultationService(ConsultationRepository consultationRepository,
                               PatientVisitRepository patientVisitRepository,
                               DoctorRepository doctorRepository,
                               ConsultationQueueRepository consultationQueueRepository,
                               WaitingQueueService waitingQueueService) {
        this.consultationRepository = consultationRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.doctorRepository = doctorRepository;
        this.consultationQueueRepository = consultationQueueRepository;
        this.waitingQueueService = waitingQueueService;
    }

    /**
     * Assigns an available doctor to a visit and creates a new consultation record.
     */
    @Transactional
    public Consultation assignDoctor(String visitId, String doctorId) {
        PatientVisit visit = patientVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + visitId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

        if (!doctor.canAcceptConsultation()) {
            throw new IllegalStateException("Doctor " + doctor.getDoctorName() + " is currently unavailable.");
        }

        doctor.allocate(visit.getPatientId(), visitId);
        doctorRepository.save(doctor);

        visit.setAssignedDoctorId(doctorId);
        visit.setVisitStatus("IN_CONSULTATION");
        patientVisitRepository.save(visit);

        // Remove from waiting queue if was queued
        consultationQueueRepository.findByVisitId(visitId).forEach(item -> {
            item.setStatus("ASSIGNED");
            consultationQueueRepository.save(item);
        });

        long count = consultationRepository.count();
        String consultationId = String.format("CON-%04d", 3001 + count);
        while (consultationRepository.existsById(consultationId)) {
            count++;
            consultationId = String.format("CON-%04d", 3001 + count);
        }

        Consultation consultation = new Consultation(consultationId, visitId, visit.getPatientId(), doctorId);
        return consultationRepository.save(consultation);
    }

    /**
     * Records or updates consultation details (symptoms, doctor observations, prescriptions).
     */
    @Transactional
    public Consultation updateConsultation(ConsultationRequestDTO dto) {
        Consultation consultation = consultationRepository.findByVisitId(dto.getVisitId())
                .orElseGet(() -> {
                    long count = consultationRepository.count();
                    String cid = String.format("CON-%04d", 3001 + count);
                    while (consultationRepository.existsById(cid)) {
                        count++;
                        cid = String.format("CON-%04d", 3001 + count);
                    }
                    PatientVisit v = patientVisitRepository.findById(dto.getVisitId()).orElse(null);
                    String pId = v != null ? v.getPatientId() : "UNKNOWN";
                    return new Consultation(cid, dto.getVisitId(), pId, dto.getDoctorId());
                });

        if (dto.getSymptoms() != null) consultation.setSymptoms(dto.getSymptoms());
        if (dto.getDoctorObservations() != null) consultation.setDoctorObservations(dto.getDoctorObservations());
        if (dto.getPrescription() != null) consultation.setPrescription(dto.getPrescription());

        if ("COMPLETED".equalsIgnoreCase(dto.getStatus())) {
            consultation.setStatus("COMPLETED");
            consultation.setCompletedAt(LocalDateTime.now());

            // Release doctor from consultation duty
            Doctor doc = doctorRepository.findById(consultation.getDoctorId()).orElse(null);
            if (doc != null) {
                doc.release();
                doctorRepository.save(doc);
                // Trigger queue re-evaluation for pending patients waiting for this doctor/specialization
                waitingQueueService.processConsultationQueue();
            }

            // Update visit status
            PatientVisit visit = patientVisitRepository.findById(dto.getVisitId()).orElse(null);
            if (visit != null && "IN_CONSULTATION".equalsIgnoreCase(visit.getVisitStatus())) {
                visit.setVisitStatus("COMPLETED");
                patientVisitRepository.save(visit);
            }
        } else {
            consultation.setStatus("IN_PROGRESS");
        }

        return consultationRepository.save(consultation);
    }

    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }

    public Optional<Consultation> getConsultationByVisitId(String visitId) {
        return consultationRepository.findByVisitId(visitId);
    }
}
