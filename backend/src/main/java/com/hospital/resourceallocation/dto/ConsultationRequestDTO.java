package com.hospital.resourceallocation.dto;

public class ConsultationRequestDTO {

    private String visitId;
    private String doctorId;
    private String symptoms;
    private String doctorObservations;
    private String prescription;
    private String status; // IN_PROGRESS, COMPLETED

    public ConsultationRequestDTO() {}

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDoctorObservations() { return doctorObservations; }
    public void setDoctorObservations(String doctorObservations) { this.doctorObservations = doctorObservations; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
