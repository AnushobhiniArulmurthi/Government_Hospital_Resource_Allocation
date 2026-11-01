package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Patient Entity
 * Unique citizen identity record preserved across multiple visits.
 * Supports Regular Patients (with previously preferred doctor recorded)
 * and New Patients (with recommended/selected doctor recorded).
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @Column(name = "patient_id", length = 20)
    private String patientId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "gender", nullable = false, length = 15)
    private String gender;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "patient_type", nullable = false, length = 20)
    private String patientType = "NEW"; // NEW, REGULAR

    @Column(name = "preferred_doctor_id", length = 20)
    private String preferredDoctorId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Patient() {}

    public Patient(String patientId, String fullName, LocalDate dateOfBirth, String gender,
                   String contactNumber, String email, String address, String patientType, String preferredDoctorId) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.age = calculateAgeFromDob(dateOfBirth);
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
        this.patientType = patientType != null ? patientType : "NEW";
        this.preferredDoctorId = preferredDoctorId;
    }

    public static int calculateAgeFromDob(LocalDate dob) {
        if (dob == null) return 0;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // Getters and Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.age = calculateAgeFromDob(dateOfBirth);
    }

    public Integer getAge() {
        if (this.dateOfBirth != null) {
            return calculateAgeFromDob(this.dateOfBirth);
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
