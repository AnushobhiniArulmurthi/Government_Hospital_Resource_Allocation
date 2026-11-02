package com.hospital.resourceallocation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unified Patient Overview DTO
 * Supplies citizen identity along with their latest medical condition,
 * clinical severity (Normal, Serious, Critical), admission state, and assigned bed.
 */
public class PatientOverviewDTO {

    private String patientId;
    private String fullName;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String contactNumber;
    private String email;
    private String address;
    private String patientType; // NEW, REGULAR
    private String medicalCondition; // Symptoms or condition notes
    private String severity; // Normal, Serious, Critical
    private String visitStatus; // REGISTERED, ADMITTED, IN_CONSULTATION, REFERRED, DISCHARGED
    private String assignedBed; // e.g. GEN-01 or null
    private String visitId;
    private LocalDateTime registrationTime;

    public PatientOverviewDTO() {}

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Integer getAge() { return age; }
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

    public String getMedicalCondition() { return medicalCondition; }
    public void setMedicalCondition(String medicalCondition) { this.medicalCondition = medicalCondition; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getVisitStatus() { return visitStatus; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }

    public String getAssignedBed() { return assignedBed; }
    public void setAssignedBed(String assignedBed) { this.assignedBed = assignedBed; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public LocalDateTime getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(LocalDateTime registrationTime) { this.registrationTime = registrationTime; }
}
