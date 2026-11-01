package com.hospital.resourceallocation.dto;

import java.util.List;
import com.hospital.resourceallocation.model.Doctor;
import com.hospital.resourceallocation.model.PatientVisit;

/**
 * Result DTO returned after checking doctor availability for a triaged visit.
 */
public class DoctorAssignmentDecisionDTO {

    private String visitId;
    private String patientId;
    private String status; // ASSIGNED, ALTERNATIVE_OFFERED, NO_DOCTOR_AVAILABLE
    private String message;
    private Doctor assignedDoctor;
    private Doctor preferredDoctor;
    private boolean preferredDoctorAvailable;
    private List<Doctor> alternativeDoctors;
    private PatientVisit visit;

    public DoctorAssignmentDecisionDTO() {}

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Doctor getAssignedDoctor() { return assignedDoctor; }
    public void setAssignedDoctor(Doctor assignedDoctor) { this.assignedDoctor = assignedDoctor; }

    public Doctor getPreferredDoctor() { return preferredDoctor; }
    public void setPreferredDoctor(Doctor preferredDoctor) { this.preferredDoctor = preferredDoctor; }

    public boolean isPreferredDoctorAvailable() { return preferredDoctorAvailable; }
    public void setPreferredDoctorAvailable(boolean preferredDoctorAvailable) { this.preferredDoctorAvailable = preferredDoctorAvailable; }

    public List<Doctor> getAlternativeDoctors() { return alternativeDoctors; }
    public void setAlternativeDoctors(List<Doctor> alternativeDoctors) { this.alternativeDoctors = alternativeDoctors; }

    public PatientVisit getVisit() { return visit; }
    public void setVisit(PatientVisit visit) { this.visit = visit; }
}
