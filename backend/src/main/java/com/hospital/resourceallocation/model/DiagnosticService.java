package com.hospital.resourceallocation.model;

import jakarta.persistence.*;

/**
 * Diagnostic Service Entity
 * Represents in-house hospital diagnostic tests (X-Ray, Ultrasound, Blood Test, Lab Test, etc.)
 */
@Entity
@Table(name = "diagnostic_services")
public class DiagnosticService {

    @Id
    @Column(name = "service_id", length = 20)
    private String serviceId;

    @Column(name = "service_code", nullable = false, unique = true, length = 30)
    private String serviceCode;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "test_type", nullable = false, length = 40)
    private String testType; // X_RAY, CT_SCAN, MRI_SCAN, ULTRASOUND, BLOOD_TEST, LAB_TEST

    @Column(name = "hospital_id", nullable = false, length = 20)
    private String hospitalId;

    @Column(name = "cost", nullable = false)
    private Double cost = 0.0;

    @Column(name = "turnaround_hours", nullable = false)
    private Integer turnaroundHours = 2;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    public DiagnosticService() {}

    public DiagnosticService(String serviceId, String serviceCode, String serviceName, String testType,
                             String hospitalId, Double cost, Integer turnaroundHours, Boolean isAvailable) {
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.testType = testType;
        this.hospitalId = hospitalId;
        this.cost = cost != null ? cost : 0.0;
        this.turnaroundHours = turnaroundHours != null ? turnaroundHours : 2;
        this.isAvailable = isAvailable != null ? isAvailable : true;
    }

    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceCode() { return serviceCode; }
    public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public Integer getTurnaroundHours() { return turnaroundHours; }
    public void setTurnaroundHours(Integer turnaroundHours) { this.turnaroundHours = turnaroundHours; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
