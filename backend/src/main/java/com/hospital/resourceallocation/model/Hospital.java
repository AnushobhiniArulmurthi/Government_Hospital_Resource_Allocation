package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hospital Entity demonstrating Composition.
 * A Hospital contains collections of Doctors, Beds, Resources, and Diagnostic Services.
 */
@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @Column(name = "hospital_id", length = 20)
    private String hospitalId;

    @Column(name = "hospital_name", nullable = false, length = 150)
    private String hospitalName;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "total_beds", nullable = false)
    private Integer totalBeds = 0;

    @Column(name = "latitude")
    private Double latitude = 0.0;

    @Column(name = "longitude")
    private Double longitude = 0.0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Composition collections (Transient or joined)
    @Transient
    private List<Doctor> doctors = new ArrayList<>();

    @Transient
    private List<Bed> beds = new ArrayList<>();

    @Transient
    private List<Resource> resources = new ArrayList<>();

    @Transient
    private List<DiagnosticService> diagnosticServices = new ArrayList<>();

    public Hospital() {}

    public Hospital(String hospitalId, String hospitalName, String locationName, Boolean isPrimary,
                    String contactNumber, String address, Integer totalBeds, Double latitude, Double longitude) {
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.locationName = locationName;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.contactNumber = contactNumber;
        this.address = address;
        this.totalBeds = totalBeds != null ? totalBeds : 0;
        this.latitude = latitude != null ? latitude : 0.0;
        this.longitude = longitude != null ? longitude : 0.0;
    }

    // Composition helper methods
    public void addDoctor(Doctor doctor) {
        this.doctors.add(doctor);
    }

    public void addBed(Bed bed) {
        this.beds.add(bed);
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
    }

    public void addDiagnosticService(DiagnosticService service) {
        this.diagnosticServices.add(service);
    }

    // Getters and Setters
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Doctor> getDoctors() { return doctors; }
    public void setDoctors(List<Doctor> doctors) { this.doctors = doctors; }

    public List<Bed> getBeds() { return beds; }
    public void setBeds(List<Bed> beds) { this.beds = beds; }

    public List<Resource> getResources() { return resources; }
    public void setResources(List<Resource> resources) { this.resources = resources; }

    public List<DiagnosticService> getDiagnosticServices() { return diagnosticServices; }
    public void setDiagnosticServices(List<DiagnosticService> diagnosticServices) { this.diagnosticServices = diagnosticServices; }
}
