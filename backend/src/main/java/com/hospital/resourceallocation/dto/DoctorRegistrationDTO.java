package com.hospital.resourceallocation.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;

/**
 * DTO for Doctor Registration by Administrator with rigorous multi-level validation.
 * Note: doctorId is NOT provided by caller; it is generated atomically by the backend.
 */
public class DoctorRegistrationDTO {

    @NotBlank(message = "Full Name is mandatory and cannot be blank")
    @Pattern(regexp = "^[A-Za-z\\s\\.]+$", message = "Full Name must contain only valid letters, spaces, and dots")
    private String doctorName;

    @NotNull(message = "Date of Birth is mandatory")
    @Past(message = "Date of Birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender selection is mandatory")
    private String gender;

    private Integer age;

    @NotBlank(message = "Specialization is mandatory")
    private String specialization;

    @NotBlank(message = "Qualification is mandatory")
    private String qualification;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid 10-digit Indian mobile number starting with 6-9")
    private String contactNumber;

    @NotBlank(message = "Email address is mandatory")
    @Email(message = "Email address format is invalid")
    private String email;

    private String dutyStatus = "ON_DUTY"; // ON_DUTY, OFF_DUTY, ON_LEAVE
    private String availabilityStatus = "AVAILABLE"; // AVAILABLE, OFF_DUTY, ON_LEAVE
    private String hospitalId = "H-001";
    private String doctorType; // GENERAL_PHYSICIAN, SPECIALIST

    @NotNull(message = "Joining Date is mandatory")
    private LocalDate dateOfJoining;

    @Min(value = 1, message = "Max inpatient capacity must be at least 1")
    @Max(value = 20, message = "Max inpatient capacity cannot exceed 20")
    private Integer maxInpatientCapacity = 6;

    public DoctorRegistrationDTO() {}

    public void validateBusinessRules() {
        if (doctorName != null) doctorName = doctorName.trim();
        if (specialization != null) specialization = specialization.trim();
        if (qualification != null) qualification = qualification.trim();
        if (contactNumber != null) contactNumber = contactNumber.trim();
        if (email != null) email = email.trim();

        if (dateOfBirth != null) {
            int calculatedAge = Period.between(dateOfBirth, LocalDate.now()).getYears();
            this.age = calculatedAge;
            if (calculatedAge < 21) {
                throw new IllegalArgumentException("Doctor must be at least 21 years of age. Calculated age: " + calculatedAge);
            }
            if (calculatedAge > 100) {
                throw new IllegalArgumentException("Invalid Date of Birth. Age exceeds reasonable limit: " + calculatedAge);
            }
        }

        if (dateOfJoining != null) {
            if (dateOfJoining.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Joining date cannot be in the future.");
            }
            if (dateOfBirth != null && dateOfJoining.isBefore(dateOfBirth.plusYears(18))) {
                throw new IllegalArgumentException("Joining date must be at least 18 years after doctor's date of birth.");
            }
        }
    }

    // Getters and Setters
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        if (dateOfBirth != null) {
            this.age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() {
        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, LocalDate.now()).getYears();
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
    public LocalDate getJoiningDate() { return dateOfJoining; }
    public void setJoiningDate(LocalDate joiningDate) { this.dateOfJoining = joiningDate; }

    public Integer getMaxInpatientCapacity() { return maxInpatientCapacity; }
    public void setMaxInpatientCapacity(Integer maxInpatientCapacity) { this.maxInpatientCapacity = maxInpatientCapacity; }
}
