package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.dto.AdmissionRequestDTO;
import com.hospital.resourceallocation.dto.DischargeRequestDTO;
import com.hospital.resourceallocation.model.*;
import com.hospital.resourceallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admission and Discharge Management Service
 * Oversees inpatient hospitalizations, atomic bed bookings, and clinical discharges with resource release.
 */
@Service
public class AdmissionDischargeService {

    private final AdmissionRepository admissionRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final BedRepository bedRepository;
    private final DoctorRepository doctorRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final DischargeHistoryRepository dischargeHistoryRepository;
    private final ResourceHistoryRepository resourceHistoryRepository;
    private final WaitingQueueService waitingQueueService;
    private final com.hospital.resourceallocation.repository.PatientRepository patientRepository;

    @Autowired
    public AdmissionDischargeService(AdmissionRepository admissionRepository,
                                     PatientVisitRepository patientVisitRepository,
                                     BedRepository bedRepository,
                                     DoctorRepository doctorRepository,
                                     ResourceRepository resourceRepository,
                                     ResourceAllocationRepository allocationRepository,
                                     DischargeHistoryRepository dischargeHistoryRepository,
                                     ResourceHistoryRepository resourceHistoryRepository,
                                     WaitingQueueService waitingQueueService,
                                     com.hospital.resourceallocation.repository.PatientRepository patientRepository) {
        this.admissionRepository = admissionRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.bedRepository = bedRepository;
        this.doctorRepository = doctorRepository;
        this.resourceRepository = resourceRepository;
        this.allocationRepository = allocationRepository;
        this.dischargeHistoryRepository = dischargeHistoryRepository;
        this.resourceHistoryRepository = resourceHistoryRepository;
        this.waitingQueueService = waitingQueueService;
        this.patientRepository = patientRepository;
    }

    /**
     * Confirms inpatient admission, allocates a specific bed atomically, and reserves mandatory resources.
     */
    @Transactional
    public Admission admitPatient(AdmissionRequestDTO dto) {
        PatientVisit visit = patientVisitRepository.findById(dto.getVisitId())
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + dto.getVisitId()));

        String hospitalId = dto.getHospitalId() != null ? dto.getHospitalId() : "H-001";
        String doctorId = dto.getDoctorId() != null ? dto.getDoctorId()
                : (visit.getAssignedDoctorId() != null ? visit.getAssignedDoctorId() : "D-001");

        // Bed allocation
        Bed allocatedBed = null;
        if (dto.getBedId() != null && !dto.getBedId().trim().isEmpty()) {
            allocatedBed = bedRepository.findByIdForUpdate(dto.getBedId())
                    .orElseThrow(() -> new IllegalArgumentException("Bed not found: " + dto.getBedId()));
        } else if (dto.getBedType() != null && !"NO_BED".equalsIgnoreCase(dto.getBedType())) {
            List<Bed> availableBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus(hospitalId, dto.getBedType(), "AVAILABLE");
            if (availableBeds.isEmpty()) {
                throw new IllegalStateException("No available " + dto.getBedType() + " bed in hospital " + hospitalId);
            }
            allocatedBed = availableBeds.get(0);
        }

        if (allocatedBed != null) {
            if (!allocatedBed.checkAvailability()) {
                throw new IllegalStateException("Bed " + allocatedBed.getBedNumber() + " is already occupied.");
            }
            allocatedBed.allocate(visit.getPatientId(), dto.getVisitId());
            bedRepository.save(allocatedBed);
            visit.setAssignedBedId(allocatedBed.getBedId());
        }

        // Doctor Inpatient Assignment
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor != null) {
            doctor.assignInpatient();
            doctorRepository.save(doctor);
        }

        // Generate Admission ID
        long count = admissionRepository.count();
        String admissionId = String.format("ADM-%04d", 6001 + count);
        while (admissionRepository.existsById(admissionId)) {
            count++;
            admissionId = String.format("ADM-%04d", 6001 + count);
        }

        Admission admission = new Admission(
                admissionId, dto.getVisitId(), visit.getPatientId(), doctorId, hospitalId,
                allocatedBed != null ? allocatedBed.getBedId() : null, dto.getNotes()
        );

        visit.setVisitStatus("ADMITTED");
        visit.setPatientCategory("Inpatient");
        patientVisitRepository.save(visit);

        return admissionRepository.save(admission);
    }

    /**
     * Executes clinical discharge: deallocates bed, releases medical equipment,
     * updates doctor inpatient caseload, deducts consumables, and triggers waiting queue processing.
     */
    @Transactional
    public DischargeHistory dischargePatient(DischargeRequestDTO dto) {
        PatientVisit visit = null;
        if (dto.getVisitId() != null && !dto.getVisitId().trim().isEmpty()) {
            visit = patientVisitRepository.findById(dto.getVisitId().trim()).orElse(null);
        }
        if (visit == null && dto.getPatientId() != null && !dto.getPatientId().trim().isEmpty()) {
            List<Admission> active = admissionRepository.findByPatientIdAndStatus(dto.getPatientId().trim(), "ACTIVE");
            if (!active.isEmpty()) {
                visit = patientVisitRepository.findById(active.get(0).getVisitId()).orElse(null);
            }
        }

        if (visit == null) {
            throw new IllegalArgumentException("Admitted patient or visit not found for: " + (dto.getPatientId() != null ? dto.getPatientId() : dto.getVisitId()));
        }

        Admission admission = admissionRepository.findByVisitId(visit.getVisitId()).orElse(null);
        if (admission == null || !"ACTIVE".equalsIgnoreCase(admission.getStatus())) {
            throw new IllegalStateException("Only currently admitted patients can be discharged. Patient " + visit.getPatientId() + " has no active inpatient admission.");
        }

        // 1. Release Bed if assigned
        String releasedBedInfo = "None";
        if (visit.getAssignedBedId() != null) {
            Bed bed = bedRepository.findByIdForUpdate(visit.getAssignedBedId()).orElse(null);
            if (bed != null) {
                releasedBedInfo = bed.getBedNumber() + " (" + bed.getBedType() + ")";
                bed.release();
                bedRepository.save(bed);
            }
            visit.setAssignedBedId(null);
        }

        // 2. Release Doctor Inpatient assignment
        if (visit.getAssignedDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(visit.getAssignedDoctorId()).orElse(null);
            if (doctor != null) {
                doctor.releaseInpatient();
                doctorRepository.save(doctor);
            }
        }

        // 3. Release Active Equipment Allocations
        List<ResourceAllocation> allocations = allocationRepository.findByVisitIdAndStatus(visit.getVisitId(), "ACTIVE");
        StringBuilder resourcesReleasedSummary = new StringBuilder();
        resourcesReleasedSummary.append("Bed: ").append(releasedBedInfo).append("; ");

        for (ResourceAllocation alloc : allocations) {
            alloc.setStatus("RELEASED");
            alloc.setReleasedAt(LocalDateTime.now());
            allocationRepository.save(alloc);

            Resource res = resourceRepository.findByIdForUpdate(alloc.getResourceId()).orElse(null);
            if (res != null) {
                res.releaseUnits(alloc.getQuantity());
                resourceRepository.save(res);

                resourceHistoryRepository.save(new ResourceHistory(
                        res.getResourceId(), res.getResourceName(), "RELEASED", alloc.getQuantity(),
                        visit.getPatientId(), visit.getVisitId(), "Deallocated upon patient discharge"
                ));
                resourcesReleasedSummary.append(res.getResourceName()).append(" (Qty: ").append(alloc.getQuantity()).append("), ");
            }
        }

        // 4. Update Admission record
        admission.setStatus("DISCHARGED");
        admission.setDischargeDate(LocalDateTime.now());
        admissionRepository.save(admission);

        // 5. Update Visit
        visit.setVisitStatus("DISCHARGED");
        visit.setDischargeTime(LocalDateTime.now());
        patientVisitRepository.save(visit);

        // 6. Record Discharge History
        long histCount = dischargeHistoryRepository.count();
        String dischargeId = String.format("DIS-%04d", 7001 + histCount);
        while (dischargeHistoryRepository.existsById(dischargeId)) {
            histCount++;
            dischargeId = String.format("DIS-%04d", 7001 + histCount);
        }

        String admId = admission.getAdmissionId();
        DischargeHistory dischargeRecord = new DischargeHistory(
                dischargeId, admId, visit.getVisitId(), visit.getPatientId(),
                dto.getDischargeSummary() != null && !dto.getDischargeSummary().trim().isEmpty() ?
                        dto.getDischargeSummary().trim() : "Patient condition stabilized. Medically discharged.",
                resourcesReleasedSummary.toString(),
                dto.getFollowUpInstructions() != null ? dto.getFollowUpInstructions() : "Follow-up in outpatient clinic after 1 week."
        );
        DischargeHistory savedHistory = dischargeHistoryRepository.save(dischargeRecord);

        // 7. Re-evaluate Resource Waiting Queue (since bed and equipment were freed)
        waitingQueueService.processResourceQueue();

        return savedHistory;
    }

    public List<com.hospital.resourceallocation.dto.AdmittedPatientDTO> getAdmittedPatients() {
        List<Admission> admissions = admissionRepository.findByStatus("ACTIVE");
        List<com.hospital.resourceallocation.dto.AdmittedPatientDTO> list = new java.util.ArrayList<>();

        for (Admission adm : admissions) {
            com.hospital.resourceallocation.dto.AdmittedPatientDTO dto = new com.hospital.resourceallocation.dto.AdmittedPatientDTO();
            dto.setAdmissionId(adm.getAdmissionId());
            dto.setVisitId(adm.getVisitId());
            dto.setPatientId(adm.getPatientId());
            dto.setAdmissionDate(adm.getAdmissionDate());

            patientRepository.findById(adm.getPatientId()).ifPresent(p -> {
                dto.setPatientName(p.getFullName());
                dto.setPatientAge(p.getAge());
                dto.setPatientGender(p.getGender());
                dto.setContactNumber(p.getContactNumber());
            });

            patientVisitRepository.findById(adm.getVisitId()).ifPresent(v -> {
                dto.setMedicalCondition(v.getNotes());
                dto.setSeverity(v.getSeverityLevel() != null ? v.getSeverityLevel() : "Normal");
            });

            if (adm.getBedId() != null) {
                bedRepository.findById(adm.getBedId()).ifPresent(b -> {
                    dto.setBedId(b.getBedId());
                    dto.setBedNumber(b.getBedNumber());
                    dto.setBedType(b.getBedType());
                    dto.setFloorWard(b.getFloorWard());
                });
            }

            if (adm.getDoctorId() != null) {
                doctorRepository.findById(adm.getDoctorId()).ifPresent(d -> {
                    dto.setDoctorId(d.getDoctorId());
                    dto.setDoctorName(d.getDoctorName());
                });
            }

            list.add(dto);
        }
        return list;
    }

    public com.hospital.resourceallocation.dto.AdmittedPatientDTO searchAdmittedPatient(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        String q = query.trim();
        List<com.hospital.resourceallocation.dto.AdmittedPatientDTO> all = getAdmittedPatients();

        return all.stream()
                .filter(a -> (a.getPatientId() != null && a.getPatientId().equalsIgnoreCase(q)) ||
                        (a.getVisitId() != null && a.getVisitId().equalsIgnoreCase(q)) ||
                        (a.getPatientName() != null && a.getPatientName().toLowerCase().contains(q.toLowerCase())))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Patient '" + q + "' is not currently admitted. Only inpatients with an active admission can be discharged."));
    }

    public List<Admission> getAllAdmissions() {
        return admissionRepository.findAll();
    }

    public List<Admission> getActiveAdmissions() {
        return admissionRepository.findByStatus("ACTIVE");
    }

    public List<DischargeHistory> getAllDischargeHistory() {
        return dischargeHistoryRepository.findAllByOrderByDischargeDateDesc();
    }
}
