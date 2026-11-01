package com.hospital.resourceallocation.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * DTO for Patient Registration and Intake.
 * Enforces strict multi-level validation:
 * - Dynamic age calculation from Date of Birth
 * - 10-digit Indian mobile number format validation
 * - Segmentation into Regular Patients and New Patients
 * - Recording of previously preferred or recommended doctor
 * - Reception staff does NOT set clinical severity level (default: PENDING_TRIAGE)
 */
public class PatientRegistrationDTO {

    // Existing patient ID (if revisiting regular patient) or empty for new citizen registration
    private String patientId;

    @NotBlank(message = "Patient Full Name is mandatory")
    @Pattern(regexp = "^[A-Za-z\\s\\.]+$", message = "Full Name must contain only valid letters, spaces, and dots")
    private String fullName;

    @NotNull(message = "Date of Birth is mandatory")
    @Past(message = "Date of Birth must be in the past")
    private LocalDate dateOfBirth;

    private Integer age;

    @NotBlank(message = "Gender selection is mandatory")
    private String gender;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit Indian mobile number starting with 6-9")
    private String contactNumber;

    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Residential address is mandatory")
    private String address;

    // Segmentation
    private String patientType = "NEW"; // NEW, REGULAR
    private String preferredDoctorId; // Recorded preferred/recommended doctor

    // Visit Info
    private String severityLevel = "PENDING_TRIAGE"; // Assigned later by doctor/triage
    private String patientCategory = "Outpatient"; // Outpatient, Diagnostic-Based, Inpatient, Emergency
    private String notes;

    // Flexible Initial Preferences
    private String preferredSpecialization;
    private String initialBedRequirement = "NO_BED";
    private List<String> initialRequestedResources;

    public PatientRegistrationDTO() {}

    public void validateBusinessRules() {
        if (fullName != null) fullName = fullName.trim();
        if (contactNumber != null) contactNumber = contactNumber.trim();
        if (address != null) address = address.trim();
        if (email != null) {
            email = email.trim();
            if (email.isEmpty()) email = null;
        }

        if (dateOfBirth != null) {
            if (dateOfBirth.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date of Birth cannot be in the future.");
            }
            int calculatedAge = Period.between(dateOfBirth, LocalDate.now()).getYears();
            this.age = calculatedAge;
            if (calculatedAge < 0 || calculatedAge > 130) {
                throw new IllegalArgumentException("Invalid Date of Birth. Calculated age out of range (0-130): " + calculatedAge);
            }
        }
    }

    // Getters and Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        if (dateOfBirth != null) {
            this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
    }

    public Integer getAge() {
        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
        return age;
    }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPatientType() { return patientType; }
    public void setPatientType(String patientType) { this.patientType = patientType; }

    public String getPreferredDoctorId() { return preferredDoctorId; }
    public void setPreferredDoctorId(String preferredDoctorId) { this.preferredDoctorId = preferredDoctorId; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }

    public String getPatientCategory() { return patientCategory; }
    public void setPatientCategory(String patientCategory) { this.patientCategory = patientCategory; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPreferredSpecialization() { return preferredSpecialization; }
    public void setPreferredSpecialization(String preferredSpecialization) { this.preferredSpecialization = preferredSpecialization; }

    public String getInitialBedRequirement() { return initialBedRequirement; }
    public void setInitialBedRequirement(String initialBedRequirement) { this.initialBedRequirement = initialBedRequirement; }

    public List<String> getInitialRequestedResources() { return initialRequestedResources; }
    public void setInitialRequestedResources(List<String> initialRequestedResources) { this.initialRequestedResources = initialRequestedResources; }
}
