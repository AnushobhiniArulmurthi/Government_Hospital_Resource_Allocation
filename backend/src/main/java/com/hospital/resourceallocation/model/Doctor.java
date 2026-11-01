package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Base Abstract/Concrete Doctor Entity.
 * Represents medical doctors as core hospital resources.
 * Demonstrates Inheritance, Polymorphism, and Encapsulation.
 */
@Entity
@Table(name = "doctors")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "doctor_type", discriminatorType = DiscriminatorType.STRING)
public class Doctor implements Allocatable {

    @Id
    @Column(name = "doctor_id", length = 20)
    private String doctorId;

    @Column(name = "doctor_name", nullable = false, length = 100)
    private String doctorName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", nullable = false, length = 15)
    private String gender = "Male";

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "specialization", nullable = false, length = 80)
    private String specialization;

    @Column(name = "qualification", nullable = false, length = 100)
    private String qualification;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "duty_status", nullable = false, length = 20)
    private String dutyStatus = "ON_DUTY"; // ON_DUTY, OFF_DUTY, ON_LEAVE

    @Column(name = "availability_status", nullable = false, length = 30)
    private String availabilityStatus = "AVAILABLE"; // AVAILABLE, IN_CONSULTATION, ASSIGNED_TO_INPATIENT, OFF_DUTY, ON_LEAVE

    @Column(name = "hospital_id", nullable = false, length = 20)
    private String hospitalId;

    @Column(name = "doctor_type", insertable = false, updatable = false)
    private String doctorType; // GENERAL_PHYSICIAN, SPECIALIST

    @Column(name = "date_of_joining", nullable = false)
    private LocalDate dateOfJoining;

    @Column(name = "max_inpatient_capacity", nullable = false)
    private Integer maxInpatientCapacity = 5;

    @Column(name = "current_inpatient_count", nullable = false)
    private Integer currentInpatientCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Doctor() {}

    public Doctor(String doctorId, String doctorName, LocalDate dateOfBirth, String gender,
                  String specialization, String qualification, String contactNumber, String email,
                  String dutyStatus, String availabilityStatus, String hospitalId,
                  LocalDate dateOfJoining, Integer maxInpatientCapacity) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.dateOfBirth = dateOfBirth != null ? dateOfBirth : LocalDate.of(1985, 1, 1);
        this.gender = gender != null ? gender : "Male";
        this.age = calculateAgeFromDob(this.dateOfBirth);
        this.specialization = specialization;
        this.qualification = qualification;
        this.contactNumber = contactNumber;
        this.email = email;
        this.dutyStatus = dutyStatus != null ? dutyStatus : "ON_DUTY";
        this.availabilityStatus = availabilityStatus != null ? availabilityStatus : "AVAILABLE";
        this.hospitalId = hospitalId;
        this.dateOfJoining = dateOfJoining != null ? dateOfJoining : LocalDate.now();
        this.maxInpatientCapacity = maxInpatientCapacity != null ? maxInpatientCapacity : 5;
        this.currentInpatientCount = 0;
        this.isActive = true;
    }

    public Doctor(String doctorId, String doctorName, String specialization, String qualification,
                  String contactNumber, String email, String dutyStatus, String availabilityStatus,
                  String hospitalId, LocalDate dateOfJoining, Integer maxInpatientCapacity) {
        this(doctorId, doctorName, LocalDate.of(1985, 1, 1), "Male", specialization, qualification,
             contactNumber, email, dutyStatus, availabilityStatus, hospitalId, dateOfJoining, maxInpatientCapacity);
    }

    public static int calculateAgeFromDob(LocalDate dob) {
        if (dob == null) return 0;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // Polymorphic specialization matching method
    public boolean matchesSpecialization(String requiredSpec) {
        if (requiredSpec == null || requiredSpec.trim().isEmpty() || "Any".equalsIgnoreCase(requiredSpec) || "General Medicine".equalsIgnoreCase(requiredSpec)) {
            return true;
        }
        return this.specialization != null && this.specialization.equalsIgnoreCase(requiredSpec.trim());
    }

    public boolean canAcceptConsultation() {
        return Boolean.TRUE.equals(this.isActive)
                && "ON_DUTY".equalsIgnoreCase(this.dutyStatus)
                && "AVAILABLE".equalsIgnoreCase(this.availabilityStatus);
    }

    public boolean canAcceptInpatient() {
        return Boolean.TRUE.equals(this.isActive)
                && "ON_DUTY".equalsIgnoreCase(this.dutyStatus)
                && this.currentInpatientCount < this.maxInpatientCapacity;
    }

    @Override
    public boolean isAvailable() {
        return canAcceptConsultation();
    }

    @Override
    public void allocate(String patientId, String visitId) {
        this.availabilityStatus = "IN_CONSULTATION";
    }

    public void assignInpatient() {
        this.currentInpatientCount++;
        if (this.currentInpatientCount >= this.maxInpatientCapacity && !"IN_CONSULTATION".equalsIgnoreCase(this.availabilityStatus)) {
            this.availabilityStatus = "ASSIGNED_TO_INPATIENT";
        }
    }

    public void releaseInpatient() {
        if (this.currentInpatientCount > 0) {
            this.currentInpatientCount--;
        }
        if (this.currentInpatientCount < this.maxInpatientCapacity && "ASSIGNED_TO_INPATIENT".equalsIgnoreCase(this.availabilityStatus)) {
            this.availabilityStatus = "AVAILABLE";
        }
    }

    @Override
    public void release() {
        if ("ON_DUTY".equalsIgnoreCase(this.dutyStatus)) {
            if (this.currentInpatientCount >= this.maxInpatientCapacity) {
                this.availabilityStatus = "ASSIGNED_TO_INPATIENT";
            } else {
                this.availabilityStatus = "AVAILABLE";
            }
        }
    }

    @Override
    public String getResourceIdentifier() {
        return this.doctorId;
    }

    public String getResourceName() {
        return "Dr. " + this.doctorName + " (" + this.specialization + ")";
    }

    @Override
    public String getResourceCategory() {
        return "DOCTOR";
    }

    // Getters and Setters
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.age = calculateAgeFromDob(dateOfBirth);
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() {
        if (this.dateOfBirth != null) {
            return calculateAgeFromDob(this.dateOfBirth);
        }
        return age;
    }
    public void setAge(Integer age) { this.age = age; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDutyStatus() { return dutyStatus; }
    public void setDutyStatus(String dutyStatus) { this.dutyStatus = dutyStatus; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getDoctorType() { return doctorType; }
    public void setDoctorType(String doctorType) { this.doctorType = doctorType; }

    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public Integer getMaxInpatientCapacity() { return maxInpatientCapacity; }
    public void setMaxInpatientCapacity(Integer maxInpatientCapacity) { this.maxInpatientCapacity = maxInpatientCapacity; }

    public Integer getCurrentInpatientCount() { return currentInpatientCount; }
    public void setCurrentInpatientCount(Integer currentInpatientCount) { this.currentInpatientCount = currentInpatientCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
