package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.algorithms.DijkstraRouter;
import com.hospital.resourceallocation.algorithms.RoutePlanner;
import com.hospital.resourceallocation.algorithms.RouteResult;
import com.hospital.resourceallocation.dto.ReferralRequestDTO;
import com.hospital.resourceallocation.model.*;
import com.hospital.resourceallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Referral Hospital Management Service
 * Employs Dijkstra's Shortest Path Algorithm to identify the fastest reachable eligible referral hospital
 * that satisfies all mandatory clinical requirements (beds, doctor specialization, equipment).
 */
@Service
public class ReferralService {

    private final RoutePlanner routePlanner;
    private final HospitalRepository hospitalRepository;
    private final BedRepository bedRepository;
    private final DoctorRepository doctorRepository;
    private final ResourceRepository resourceRepository;
    private final ReferralRepository referralRepository;
    private final PatientVisitRepository patientVisitRepository;
    private final WaitingQueueService waitingQueueService;
    private final PatientRepository patientRepository;

    @Autowired
    public ReferralService(DijkstraRouter routePlanner,
                           HospitalRepository hospitalRepository,
                           BedRepository bedRepository,
                           DoctorRepository doctorRepository,
                           ResourceRepository resourceRepository,
                           ReferralRepository referralRepository,
                           PatientVisitRepository patientVisitRepository,
                           WaitingQueueService waitingQueueService,
                           PatientRepository patientRepository) {
        this.routePlanner = routePlanner;
        this.hospitalRepository = hospitalRepository;
        this.bedRepository = bedRepository;
        this.doctorRepository = doctorRepository;
        this.resourceRepository = resourceRepository;
        this.referralRepository = referralRepository;
        this.patientVisitRepository = patientVisitRepository;
        this.waitingQueueService = waitingQueueService;
        this.patientRepository = patientRepository;
    }

    /**
     * Checks if a candidate referral hospital satisfies all mandatory clinical requirements.
     */
    public boolean isHospitalEligible(Hospital hospital, String requiredBedType,
                                     String requiredSpecialization, List<String> requiredResources) {
        String hid = hospital.getHospitalId();

        // 1. Bed Availability Check
        if (requiredBedType != null && !"NO_BED".equalsIgnoreCase(requiredBedType) && !requiredBedType.trim().isEmpty()) {
            List<Bed> availableBeds = bedRepository.findByHospitalIdAndBedTypeAndStatus(hid, requiredBedType, "AVAILABLE");
            if (availableBeds.isEmpty()) {
                return false;
            }
        }

        // 2. Doctor Availability & Specialization Check
        if (requiredSpecialization != null && !requiredSpecialization.trim().isEmpty() && !"Any".equalsIgnoreCase(requiredSpecialization)) {
            List<Doctor> doctors = doctorRepository.findByHospitalId(hid);
            boolean hasDoctor = doctors.stream()
                    .anyMatch(d -> d.matchesSpecialization(requiredSpecialization) && d.canAcceptConsultation());
            if (!hasDoctor) {
                return false;
            }
        }

        // 3. Equipment / Resource Check
        if (requiredResources != null && !requiredResources.isEmpty()) {
            for (String resName : requiredResources) {
                Optional<Resource> resOpt = resourceRepository.findByHospitalIdAndResourceNameIgnoreCase(hid, resName);
                if (resOpt.isEmpty() || !resOpt.get().checkAvailability(1)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Finds the fastest reachable eligible referral hospital using Dijkstra's algorithm.
     */
    public RouteResult findFastestEligibleHospital(String originHospitalId, String requiredBedType,
                                                  String requiredSpecialization, List<String> requiredResources) {
        Hospital originHospital = hospitalRepository.findById(originHospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Origin hospital not found: " + originHospitalId));

        List<Hospital> referralCandidates = hospitalRepository.findByIsPrimaryFalse();

        Hospital bestHospital = null;
        RouteResult bestRoute = null;
        int minTravelTime = Integer.MAX_VALUE;

        for (Hospital candidate : referralCandidates) {
            // Check clinical eligibility first
            if (isHospitalEligible(candidate, requiredBedType, requiredSpecialization, requiredResources)) {
                // Compute shortest travel-time path via Dijkstra
                RouteResult route = routePlanner.findShortestRoute(originHospital.getLocationName(), candidate.getLocationName());
                if (route.getTotalTravelTimeMinutes() < minTravelTime) {
                    minTravelTime = route.getTotalTravelTimeMinutes();
                    bestHospital = candidate;
                    bestRoute = route;
                }
            }
        }

        if (bestHospital != null && bestRoute != null) {
            bestRoute.setDestination(bestHospital.getHospitalName() + " (" + bestHospital.getLocationName() + ")");
            return bestRoute;
        }

        return null; // No eligible referral hospital available
    }

    /**
     * Confirms patient referral to the fastest eligible hospital and reserves required resources.
     */
    @Transactional
    public Referral processReferral(ReferralRequestDTO dto) {
        PatientVisit visit = patientVisitRepository.findById(dto.getVisitId())
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + dto.getVisitId()));

        String fromHospId = dto.getFromHospitalId() != null ? dto.getFromHospitalId() : "H-001";
        Hospital fromHosp = hospitalRepository.findById(fromHospId).orElse(null);
        String originLocation = fromHosp != null ? fromHosp.getLocationName() : "Nalam Central";

        Hospital targetHospital = null;
        RouteResult routeResult = null;

        if (dto.getTargetHospitalId() != null && !dto.getTargetHospitalId().trim().isEmpty()) {
            targetHospital = hospitalRepository.findById(dto.getTargetHospitalId()).orElse(null);
            if (targetHospital != null) {
                routeResult = routePlanner.findShortestRoute(originLocation, targetHospital.getLocationName());
            }
        }

        if (targetHospital == null || routeResult == null) {
            // Auto-discover fastest eligible hospital using Dijkstra's algorithm
            routeResult = findFastestEligibleHospital(
                    fromHospId, dto.getRequiredBedType(), dto.getRequiredSpecialization(), dto.getRequiredResources()
            );

            if (routeResult == null) {
                // No eligible referral hospital found! Place in Resource Waiting Queue
                waitingQueueService.enqueueResourceRequest(
                        dto.getVisitId(), visit.getPatientId(), visit.getSeverityLevel(),
                        dto.getRequiredBedType(), dto.getRequiredSpecialization(),
                        dto.getRequiredResources() != null ? String.join(", ", dto.getRequiredResources()) : "All"
                );
                throw new IllegalStateException("No eligible referral hospital satisfies the complete mandatory requirement. Patient has been enrolled in the Resource Allocation Waiting Queue.");
            }

            // Identify hospital by location from route
            String destLocation = routeResult.getPathNodes().get(routeResult.getPathNodes().size() - 1);
            targetHospital = hospitalRepository.findByLocationName(destLocation)
                    .orElse(hospitalRepository.findByIsPrimaryFalse().get(0));
        }

        // Generate Referral ID
        long count = referralRepository.count();
        String referralId = String.format("REF-%04d", 8001 + count);
        while (referralRepository.existsById(referralId)) {
            count++;
            referralId = String.format("REF-%04d", 8001 + count);
        }

        Referral referral = new Referral(
                referralId, dto.getVisitId(), visit.getPatientId(), fromHospId, targetHospital.getHospitalId(),
                dto.getReason() != null ? dto.getReason() : "Clinical escalation to higher specialty center",
                routeResult.getTotalTravelTimeMinutes(),
                routeResult.getFormattedPath()
        );

        visit.setVisitStatus("REFERRED");
        patientVisitRepository.save(visit);

        return referralRepository.save(referral);
    }

    public List<Referral> getAllReferrals() {
        return referralRepository.findAll();
    }

    public List<com.hospital.resourceallocation.dto.ReferralDetailDTO> getEnrichedReferrals() {
        List<Referral> referrals = referralRepository.findAll();
        List<com.hospital.resourceallocation.dto.ReferralDetailDTO> list = new java.util.ArrayList<>();

        for (Referral r : referrals) {
            com.hospital.resourceallocation.dto.ReferralDetailDTO dto = new com.hospital.resourceallocation.dto.ReferralDetailDTO();
            dto.setReferralId(r.getReferralId());
            dto.setVisitId(r.getVisitId());
            dto.setPatientId(r.getPatientId());
            dto.setFromHospitalId(r.getFromHospitalId());
            dto.setToHospitalId(r.getToHospitalId());
            dto.setReason(r.getReason());
            dto.setTravelTimeMinutes(r.getTravelTimeMinutes());
            dto.setRoutePath(r.getRoutePath());
            dto.setStatus(r.getStatus());
            dto.setCreatedAt(r.getCreatedAt());

            patientRepository.findById(r.getPatientId()).ifPresent(p -> {
                dto.setPatientName(p.getFullName());
                dto.setPatientAge(p.getAge());
                dto.setPatientGender(p.getGender());
                dto.setPatientContact(p.getContactNumber());
            });

            patientVisitRepository.findById(r.getVisitId()).ifPresent(v -> {
                dto.setSeverity(v.getSeverityLevel() != null ? v.getSeverityLevel() : "Normal");
                dto.setMedicalCondition(v.getNotes());
            });

            hospitalRepository.findById(r.getToHospitalId()).ifPresent(h -> {
                dto.setToHospitalName(h.getHospitalName());
                dto.setToHospitalContact(h.getContactNumber());
                dto.setToHospitalAddress(h.getAddress());
            });

            list.add(dto);
        }
        return list;
    }

    @Transactional
    public Referral approveReferral(String referralId) {
        Referral ref = referralRepository.findById(referralId)
                .orElseThrow(() -> new IllegalArgumentException("Referral not found: " + referralId));
        ref.setStatus("CONFIRMED");
        Referral saved = referralRepository.save(ref);

        patientVisitRepository.findById(ref.getVisitId()).ifPresent(v -> {
            v.setVisitStatus("REFERRED");
            patientVisitRepository.save(v);
        });

        return saved;
    }

    @Transactional
    public void moveReferralToWaitingQueue(String referralId) {
        Referral ref = referralRepository.findById(referralId)
                .orElseThrow(() -> new IllegalArgumentException("Referral not found: " + referralId));
        ref.setStatus("CANCELLED");
        referralRepository.save(ref);

        PatientVisit visit = patientVisitRepository.findById(ref.getVisitId())
                .orElseThrow(() -> new IllegalArgumentException("Visit not found: " + ref.getVisitId()));

        waitingQueueService.enqueueResourceRequest(
                visit.getVisitId(), visit.getPatientId(), visit.getSeverityLevel(),
                "GENERAL", null, ref.getReason()
        );

        visit.setVisitStatus("WAITING_FOR_RESOURCE");
        patientVisitRepository.save(visit);
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }
}
