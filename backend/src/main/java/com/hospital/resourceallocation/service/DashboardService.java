package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.dto.DashboardStatsDTO;
import com.hospital.resourceallocation.model.Doctor;
import com.hospital.resourceallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dashboard Service
 * Computes all dynamic hospital statistics directly from MySQL database entities.
 */
@Service
public class DashboardService {

    private final PatientRepository patientRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final ResourceRepository resourceRepository;
    private final DiagnosticServiceRepository diagnosticServiceRepository;
    private final ReferralRepository referralRepository;
    private final ConsultationQueueRepository consultationQueueRepository;
    private final ResourceQueueRepository resourceQueueRepository;
    private final AdmissionRepository admissionRepository;

    @Autowired
    public DashboardService(PatientRepository patientRepository,
                            PatientVisitRepository patientVisitRepository,
                            DoctorRepository doctorRepository,
                            BedRepository bedRepository,
                            ResourceRepository resourceRepository,
                            DiagnosticServiceRepository diagnosticServiceRepository,
                            ReferralRepository referralRepository,
                            ConsultationQueueRepository consultationQueueRepository,
                            ResourceQueueRepository resourceQueueRepository,
                            AdmissionRepository admissionRepository) {
        this.patientRepository = patientRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.resourceRepository = resourceRepository;
        this.diagnosticServiceRepository = diagnosticServiceRepository;
        this.referralRepository = referralRepository;
        this.consultationQueueRepository = consultationQueueRepository;
        this.resourceQueueRepository = resourceQueueRepository;
        this.admissionRepository = admissionRepository;
    }

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        String primaryHospitalId = "H-001";

        // Patient Stats
        dto.setTotalRegisteredPatients(patientRepository.countTotalPatients());
        dto.setCriticalPatients(patientVisitRepository.countCriticalVisits());
        dto.setSeriousPatients(patientVisitRepository.countSeriousVisits());
        dto.setMinorPatients(patientVisitRepository.countMinorVisits());
        dto.setWaitingForConsultation(consultationQueueRepository.findByStatusOrderByPriorityScoreAscArrivalTimeAsc("WAITING").size());
        dto.setUnderTreatment(patientVisitRepository.countUnderTreatment());
        dto.setAdmittedPatients(admissionRepository.countActiveAdmissions());
        dto.setDischargedPatients(patientVisitRepository.countDischarged());

        // Doctor Stats (Active staff only)
        List<Doctor> activeDoctors = doctorRepository.findByIsActiveTrue();
        dto.setTotalDoctors(activeDoctors.size());
        dto.setAvailableDoctors(activeDoctors.stream().filter(Doctor::canAcceptConsultation).count());
        dto.setConsultingDoctors(activeDoctors.stream().filter(d -> "IN_CONSULTATION".equalsIgnoreCase(d.getAvailabilityStatus())).count());
        dto.setAssignedToInpatients(activeDoctors.stream().filter(d -> d.getCurrentInpatientCount() > 0).count());
        dto.setOffDutyOrLeave(activeDoctors.stream().filter(d -> "OFF_DUTY".equalsIgnoreCase(d.getDutyStatus()) || "ON_LEAVE".equalsIgnoreCase(d.getDutyStatus())).count());

        // Bed Stats
        dto.setTotalBeds(bedRepository.findByHospitalId(primaryHospitalId).size());
        dto.setAvailableBeds(bedRepository.countAvailableBeds(primaryHospitalId));
        dto.setOccupiedBeds(bedRepository.countOccupiedBeds(primaryHospitalId));
        dto.setAvailableGeneralBeds(bedRepository.countAvailableByType(primaryHospitalId, "GENERAL"));
        dto.setAvailableIcuBeds(bedRepository.countAvailableByType(primaryHospitalId, "ICU"));
        dto.setAvailableEmergencyBeds(bedRepository.countAvailableByType(primaryHospitalId, "EMERGENCY"));

        // Resource Stats
        dto.setAvailableEquipment(resourceRepository.countAvailableEquipment(primaryHospitalId));
        dto.setAvailableDiagnosticServices(diagnosticServiceRepository.findByHospitalIdAndIsAvailableTrue(primaryHospitalId).size());
        Long supplyStock = resourceRepository.sumMedicalSupplies(primaryHospitalId);
        dto.setMedicalSupplyStock(supplyStock != null ? supplyStock : 0);
        dto.setPendingResourceRequests(resourceQueueRepository.findByStatusOrderByPriorityScoreAscArrivalTimeAsc("WAITING").size());

        // Referral Stats
        dto.setTotalReferrals(referralRepository.count());
        dto.setPendingReferrals(referralRepository.countPendingReferrals());
        dto.setCompletedReferrals(referralRepository.countCompletedReferrals());

        return dto;
    }
}
