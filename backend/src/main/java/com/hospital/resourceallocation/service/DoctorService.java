package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.dto.DoctorRegistrationDTO;
import com.hospital.resourceallocation.model.Doctor;
import com.hospital.resourceallocation.model.GeneralPhysician;
import com.hospital.resourceallocation.model.SpecialistDoctor;
import com.hospital.resourceallocation.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Doctor Management Service
 * Implements atomic database sequence-backed Doctor ID generation (D-001, D-002...),
 * duty status toggling (On Duty / Off Duty), permanent deactivation (preserving historical records),
 * and availability filtering.
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /**
     * Atomically generates the next unique Doctor ID using the database-backed sequence table
     * with pessimistic row locking (SELECT ... FOR UPDATE) to ensure concurrency safety.
     * Guaranteed never to produce duplicates or re-use historical IDs.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized String generateNextDoctorId() {
        Long currentVal = doctorRepository.getNextSequenceValForUpdate();
        if (currentVal == null) {
            currentVal = 0L;
        }
        doctorRepository.incrementDoctorSequence();
        long nextVal = currentVal + 1;
        return String.format("D-%03d", nextVal);
    }

    /**
     * Registers a new doctor with rigorous backend business rule validation.
     */
    @Transactional
    public Doctor registerDoctor(DoctorRegistrationDTO dto) {
        dto.validateBusinessRules();

        String generatedId = generateNextDoctorId();
        String doctorType = dto.getDoctorType();
        if (doctorType == null || doctorType.trim().isEmpty()) {
            if ("General Medicine".equalsIgnoreCase(dto.getSpecialization())
                    || "General Practice".equalsIgnoreCase(dto.getSpecialization())) {
                doctorType = "GENERAL_PHYSICIAN";
            } else {
                doctorType = "SPECIALIST";
            }
        }

        LocalDate joiningDate = dto.getDateOfJoining() != null ? dto.getDateOfJoining() : LocalDate.now();
        Integer maxCapacity = dto.getMaxInpatientCapacity() != null ? dto.getMaxInpatientCapacity() : 6;
        String dutyStatus = dto.getDutyStatus() != null ? dto.getDutyStatus() : "ON_DUTY";
        String availStatus = dto.getAvailabilityStatus() != null ? dto.getAvailabilityStatus() : "AVAILABLE";
        String hospitalId = dto.getHospitalId() != null ? dto.getHospitalId() : "H-001";
        LocalDate dob = dto.getDateOfBirth() != null ? dto.getDateOfBirth() : LocalDate.of(1985, 1, 1);
        String gender = dto.getGender() != null ? dto.getGender() : "Male";

        Doctor doctor;
        if ("GENERAL_PHYSICIAN".equalsIgnoreCase(doctorType)) {
            doctor = new GeneralPhysician(
                    generatedId, dto.getDoctorName(), dto.getSpecialization(), dto.getQualification(),
                    dto.getContactNumber(), dto.getEmail(), dutyStatus, availStatus,
                    hospitalId, joiningDate, maxCapacity
            );
        } else {
            doctor = new SpecialistDoctor(
                    generatedId, dto.getDoctorName(), dto.getSpecialization(), dto.getQualification(),
                    dto.getContactNumber(), dto.getEmail(), dutyStatus, availStatus,
                    hospitalId, joiningDate, maxCapacity
            );
        }
        doctor.setDateOfBirth(dob);
        doctor.setGender(gender);
        doctor.setAge(Doctor.calculateAgeFromDob(dob));

        return doctorRepository.save(doctor);
    }

    /**
     * Retrieves all active doctors. Deactivated doctors are excluded by default.
     */
    public List<Doctor> getAllActiveDoctors() {
        return doctorRepository.findByIsActiveTrue();
    }

    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findByIsActiveTrue();
    }

    public List<Doctor> getAllDoctors(boolean includeDeactivated) {
        if (includeDeactivated) {
            return doctorRepository.findAll();
        }
        return doctorRepository.findByIsActiveTrue();
    }

    public List<Doctor> getAllDoctors() {
        return getAllActiveDoctors();
    }

    public Optional<Doctor> getDoctorById(String doctorId) {
        return doctorRepository.findById(doctorId);
    }

    public List<Doctor> searchByName(String name) {
        return doctorRepository.findByIsActiveTrueAndDoctorNameContainingIgnoreCase(name);
    }

    public List<Doctor> filterBySpecialization(String specialization) {
        return doctorRepository.findByIsActiveTrueAndSpecializationContainingIgnoreCase(specialization);
    }

    public List<Doctor> getAvailableDoctors(String hospitalId) {
        return doctorRepository.findByIsActiveTrueAndHospitalIdAndDutyStatusAndAvailabilityStatus(hospitalId, "ON_DUTY", "AVAILABLE");
    }

    public List<Doctor> findSuitableAvailableDoctors(String hospitalId, String requiredSpecialization) {
        List<Doctor> onDutyAvailable = getAvailableDoctors(hospitalId);
        return onDutyAvailable.stream()
                .filter(d -> d.matchesSpecialization(requiredSpecialization))
                .toList();
    }

    @Transactional
    public Doctor updateDoctor(String doctorId, DoctorRegistrationDTO dto) {
        dto.validateBusinessRules();

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

        if (dto.getDoctorName() != null) doctor.setDoctorName(dto.getDoctorName());
        if (dto.getDateOfBirth() != null) doctor.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) doctor.setGender(dto.getGender());
        if (dto.getSpecialization() != null) doctor.setSpecialization(dto.getSpecialization());
        if (dto.getQualification() != null) doctor.setQualification(dto.getQualification());
        if (dto.getContactNumber() != null) doctor.setContactNumber(dto.getContactNumber());
        if (dto.getEmail() != null) doctor.setEmail(dto.getEmail());
        if (dto.getDutyStatus() != null) doctor.setDutyStatus(dto.getDutyStatus());
        if (dto.getAvailabilityStatus() != null) doctor.setAvailabilityStatus(dto.getAvailabilityStatus());
        if (dto.getMaxInpatientCapacity() != null) doctor.setMaxInpatientCapacity(dto.getMaxInpatientCapacity());

        return doctorRepository.save(doctor);
    }

    /**
     * Clear single duty-status control: toggles between ON_DUTY and OFF_DUTY.
     * Off-duty doctors remain registered in system but cannot receive new assignments.
     */
    @Transactional
    public Doctor toggleDutyStatus(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new IllegalStateException("Cannot change duty status of a deactivated doctor.");
        }

        String nextStatus = "ON_DUTY".equalsIgnoreCase(doctor.getDutyStatus()) ? "OFF_DUTY" : "ON_DUTY";
        return updateDutyStatus(doctorId, nextStatus);
    }

    @Transactional
    public Doctor updateDutyStatus(String doctorId, String dutyStatus) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new IllegalStateException("Cannot change duty status of a deactivated doctor.");
        }

        doctor.setDutyStatus(dutyStatus);
        if ("OFF_DUTY".equalsIgnoreCase(dutyStatus) || "ON_LEAVE".equalsIgnoreCase(dutyStatus)) {
            doctor.setAvailabilityStatus(dutyStatus);
        } else if ("ON_DUTY".equalsIgnoreCase(dutyStatus)) {
            doctor.release();
        }
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateAvailabilityStatus(String doctorId, String availabilityStatus) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        doctor.setAvailabilityStatus(availabilityStatus);
        return doctorRepository.save(doctor);
    }

    /**
     * Deactivate doctor represents a doctor who has resigned or permanently left the hospital.
     * Deactivated doctors disappear from active doctor lists and assignment options.
     * All historical consultation and patient records are preserved.
     */
    @Transactional
    public Doctor deactivateDoctor(String doctorId) {
        return deactivateDoctor(doctorId, null);
    }

    @Transactional
    public Doctor deactivateDoctor(String doctorId, String reason) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        doctor.setIsActive(false);
        doctor.setDutyStatus("OFF_DUTY");
        doctor.setAvailabilityStatus("DEACTIVATED");
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void reserveForConsultation(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        doctor.allocate(null, null); // sets availabilityStatus = IN_CONSULTATION
        doctorRepository.save(doctor);
    }

    @Transactional
    public void releaseFromConsultation(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        doctor.release();
        doctorRepository.save(doctor);
    }

    @Transactional
    public void assignInpatientCare(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        doctor.assignInpatient();
        doctorRepository.save(doctor);
    }

    @Transactional
    public void releaseInpatientCare(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        doctor.releaseInpatient();
        doctorRepository.save(doctor);
    }
}
