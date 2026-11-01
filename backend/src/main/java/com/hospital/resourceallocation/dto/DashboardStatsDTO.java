package com.hospital.resourceallocation.dto;


/**
 * Data Transfer Object containing all dynamic Hospital Dashboard Metrics.
 */
public class DashboardStatsDTO {

    // Patient Statistics
    private long totalRegisteredPatients;
    private long criticalPatients;
    private long seriousPatients;
    private long minorPatients;
    private long waitingForConsultation;
    private long underTreatment;
    private long admittedPatients;
    private long dischargedPatients;

    // Doctor Statistics
    private long totalDoctors;
    private long availableDoctors;
    private long consultingDoctors;
    private long assignedToInpatients;
    private long offDutyOrLeave;

    // Bed Statistics
    private long totalBeds;
    private long availableBeds;
    private long occupiedBeds;
    private long availableGeneralBeds;
    private long availableIcuBeds;
    private long availableEmergencyBeds;

    // Resource Statistics
    private long availableEquipment;
    private long availableDiagnosticServices;
    private long medicalSupplyStock;
    private long pendingResourceRequests;

    // Referral Statistics
    private long totalReferrals;
    private long pendingReferrals;
    private long completedReferrals;

    public DashboardStatsDTO() {}

    // Getters and Setters
    public long getTotalRegisteredPatients() { return totalRegisteredPatients; }
    public void setTotalRegisteredPatients(long totalRegisteredPatients) { this.totalRegisteredPatients = totalRegisteredPatients; }

    public long getCriticalPatients() { return criticalPatients; }
    public void setCriticalPatients(long criticalPatients) { this.criticalPatients = criticalPatients; }

    public long getSeriousPatients() { return seriousPatients; }
    public void setSeriousPatients(long seriousPatients) { this.seriousPatients = seriousPatients; }

    public long getMinorPatients() { return minorPatients; }
    public void setMinorPatients(long minorPatients) { this.minorPatients = minorPatients; }

    public long getWaitingForConsultation() { return waitingForConsultation; }
    public void setWaitingForConsultation(long waitingForConsultation) { this.waitingForConsultation = waitingForConsultation; }

    public long getUnderTreatment() { return underTreatment; }
    public void setUnderTreatment(long underTreatment) { this.underTreatment = underTreatment; }

    public long getAdmittedPatients() { return admittedPatients; }
    public void setAdmittedPatients(long admittedPatients) { this.admittedPatients = admittedPatients; }

    public long getDischargedPatients() { return dischargedPatients; }
    public void setDischargedPatients(long dischargedPatients) { this.dischargedPatients = dischargedPatients; }

    public long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(long totalDoctors) { this.totalDoctors = totalDoctors; }

    public long getAvailableDoctors() { return availableDoctors; }
    public void setAvailableDoctors(long availableDoctors) { this.availableDoctors = availableDoctors; }

    public long getConsultingDoctors() { return consultingDoctors; }
    public void setConsultingDoctors(long consultingDoctors) { this.consultingDoctors = consultingDoctors; }

    public long getAssignedToInpatients() { return assignedToInpatients; }
    public void setAssignedToInpatients(long assignedToInpatients) { this.assignedToInpatients = assignedToInpatients; }

    public long getOffDutyOrLeave() { return offDutyOrLeave; }
    public void setOffDutyOrLeave(long offDutyOrLeave) { this.offDutyOrLeave = offDutyOrLeave; }

    public long getTotalBeds() { return totalBeds; }
    public void setTotalBeds(long totalBeds) { this.totalBeds = totalBeds; }

    public long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(long availableBeds) { this.availableBeds = availableBeds; }

    public long getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(long occupiedBeds) { this.occupiedBeds = occupiedBeds; }

    public long getAvailableGeneralBeds() { return availableGeneralBeds; }
    public void setAvailableGeneralBeds(long availableGeneralBeds) { this.availableGeneralBeds = availableGeneralBeds; }

    public long getAvailableIcuBeds() { return availableIcuBeds; }
    public void setAvailableIcuBeds(long availableIcuBeds) { this.availableIcuBeds = availableIcuBeds; }

    public long getAvailableEmergencyBeds() { return availableEmergencyBeds; }
    public void setAvailableEmergencyBeds(long availableEmergencyBeds) { this.availableEmergencyBeds = availableEmergencyBeds; }

    public long getAvailableEquipment() { return availableEquipment; }
    public void setAvailableEquipment(long availableEquipment) { this.availableEquipment = availableEquipment; }

    public long getAvailableDiagnosticServices() { return availableDiagnosticServices; }
    public void setAvailableDiagnosticServices(long availableDiagnosticServices) { this.availableDiagnosticServices = availableDiagnosticServices; }

    public long getMedicalSupplyStock() { return medicalSupplyStock; }
    public void setMedicalSupplyStock(long medicalSupplyStock) { this.medicalSupplyStock = medicalSupplyStock; }

    public long getPendingResourceRequests() { return pendingResourceRequests; }
    public void setPendingResourceRequests(long pendingResourceRequests) { this.pendingResourceRequests = pendingResourceRequests; }

    public long getTotalReferrals() { return totalReferrals; }
    public void setTotalReferrals(long totalReferrals) { this.totalReferrals = totalReferrals; }

    public long getPendingReferrals() { return pendingReferrals; }
    public void setPendingReferrals(long pendingReferrals) { this.pendingReferrals = pendingReferrals; }

    public long getCompletedReferrals() { return completedReferrals; }
    public void setCompletedReferrals(long completedReferrals) { this.completedReferrals = completedReferrals; }
}
