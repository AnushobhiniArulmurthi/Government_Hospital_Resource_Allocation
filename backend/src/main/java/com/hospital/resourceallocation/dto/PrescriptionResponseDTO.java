package com.hospital.resourceallocation.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO returned after Doctor prescribes resources.
 * Reflects whether resources were allocated locally,
 * referred to a registered referral hospital via Dijkstra,
 * or placed into the Severity-Based Priority Queue.
 */
public class PrescriptionResponseDTO {

    private String status; // "ALLOCATED", "REFERRAL_INITIATED", "WAITING_LIST"
    private String message;
    private String patientId;
    private String patientName;
    private String visitId;
    private String severity;

    // Local Allocation Details
    private String allocatedBedNumber;
    private String allocatedBedType;
    private String admissionId;
    private List<String> allocatedResources = new ArrayList<>();
    private List<String> diagnosticOrders = new ArrayList<>();

    // Referral Details
    private String referralId;
    private String referralHospitalId;
    private String referralHospitalName;
    private String referralHospitalContact;
    private String referralHospitalAddress;
    private Integer referralTravelTimeMinutes;
    private String referralRoute;

    // Waiting Queue Details
    private String queueId;
    private Integer priorityScore;

    public PrescriptionResponseDTO() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getAllocatedBedNumber() { return allocatedBedNumber; }
    public void setAllocatedBedNumber(String allocatedBedNumber) { this.allocatedBedNumber = allocatedBedNumber; }

    public String getAllocatedBedType() { return allocatedBedType; }
    public void setAllocatedBedType(String allocatedBedType) { this.allocatedBedType = allocatedBedType; }

    public String getAdmissionId() { return admissionId; }
    public void setAdmissionId(String admissionId) { this.admissionId = admissionId; }

    public List<String> getAllocatedResources() { return allocatedResources; }
    public void setAllocatedResources(List<String> allocatedResources) { this.allocatedResources = allocatedResources; }

    public List<String> getDiagnosticOrders() { return diagnosticOrders; }
    public void setDiagnosticOrders(List<String> diagnosticOrders) { this.diagnosticOrders = diagnosticOrders; }

    public String getReferralId() { return referralId; }
    public void setReferralId(String referralId) { this.referralId = referralId; }

    public String getReferralHospitalId() { return referralHospitalId; }
    public void setReferralHospitalId(String referralHospitalId) { this.referralHospitalId = referralHospitalId; }

    public String getReferralHospitalName() { return referralHospitalName; }
    public void setReferralHospitalName(String referralHospitalName) { this.referralHospitalName = referralHospitalName; }

    public String getReferralHospitalContact() { return referralHospitalContact; }
    public void setReferralHospitalContact(String referralHospitalContact) { this.referralHospitalContact = referralHospitalContact; }

    public String getReferralHospitalAddress() { return referralHospitalAddress; }
    public void setReferralHospitalAddress(String referralHospitalAddress) { this.referralHospitalAddress = referralHospitalAddress; }

    public Integer getReferralTravelTimeMinutes() { return referralTravelTimeMinutes; }
    public void setReferralTravelTimeMinutes(Integer referralTravelTimeMinutes) { this.referralTravelTimeMinutes = referralTravelTimeMinutes; }

    public String getReferralRoute() { return referralRoute; }
    public void setReferralRoute(String referralRoute) { this.referralRoute = referralRoute; }

    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }

    public Integer getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Integer priorityScore) { this.priorityScore = priorityScore; }
}
