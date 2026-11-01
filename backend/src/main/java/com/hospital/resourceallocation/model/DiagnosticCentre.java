package com.hospital.resourceallocation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Diagnostic Centre Entity
 * Represents external diagnostic facilities outside the primary government hospital
 * for scans like MRI, CT, PET scans, etc.
 */
@Entity
@Table(name = "diagnostic_centres")
public class DiagnosticCentre {

    @Id
    @Column(name = "centre_id", length = 20)
    private String centreId;

    @Column(name = "centre_name", nullable = false, length = 150)
    private String centreName;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "available_services", columnDefinition = "TEXT", nullable = false)
    private String availableServices;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public DiagnosticCentre() {}

    public DiagnosticCentre(String centreId, String centreName, String locationName,
                            String contactNumber, String address, String availableServices) {
        this.centreId = centreId;
        this.centreName = centreName;
        this.locationName = locationName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.availableServices = availableServices;
        this.isActive = true;
    }

    public boolean supportsTest(String testType) {
        if (testType == null || this.availableServices == null) return false;
        return this.availableServices.toLowerCase().contains(testType.toLowerCase().replace("_", " "));
    }

    // Getters and Setters
    public String getCentreId() { return centreId; }
    public void setCentreId(String centreId) { this.centreId = centreId; }

    public String getCentreName() { return centreName; }
    public void setCentreName(String centreName) { this.centreName = centreName; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAvailableServices() { return availableServices; }
    public void setAvailableServices(String availableServices) { this.availableServices = availableServices; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
