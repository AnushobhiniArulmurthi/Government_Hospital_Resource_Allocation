package com.hospital.resourceallocation.service;

import com.hospital.resourceallocation.model.Bed;
import com.hospital.resourceallocation.model.EmergencyBed;
import com.hospital.resourceallocation.model.GeneralBed;
import com.hospital.resourceallocation.model.ICUBed;
import com.hospital.resourceallocation.repository.BedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Bed Management Service
 * Manages General, ICU, and Emergency beds with pessimistic row locking for concurrent safety.
 */
@Service
public class BedService {

    private final BedRepository bedRepository;

    @Autowired
    public BedService(BedRepository bedRepository) {
        this.bedRepository = bedRepository;
    }

    public List<Bed> getAllBeds() {
        return bedRepository.findAll();
    }

    public List<Bed> getBedsByHospital(String hospitalId) {
        return bedRepository.findByHospitalId(hospitalId);
    }

    public List<Bed> getAvailableBeds(String hospitalId) {
        return bedRepository.findByHospitalIdAndStatus(hospitalId, "AVAILABLE");
    }

    public List<Bed> getOccupiedBeds(String hospitalId) {
        return bedRepository.findByHospitalIdAndStatus(hospitalId, "OCCUPIED");
    }

    public List<Bed> getAvailableBedsByType(String hospitalId, String bedType) {
        return bedRepository.findByHospitalIdAndBedTypeAndStatus(hospitalId, bedType, "AVAILABLE");
    }

    public Optional<Bed> getBedById(String bedId) {
        return bedRepository.findById(bedId);
    }

    @Transactional
    public Bed addBed(String bedId, String bedNumber, String bedType, String hospitalId, String floorWard, Double dailyCharge) {
        Bed bed;
        if ("ICU".equalsIgnoreCase(bedType)) {
            bed = new ICUBed(bedId, bedNumber, hospitalId, "AVAILABLE", floorWard, dailyCharge);
        } else if ("EMERGENCY".equalsIgnoreCase(bedType)) {
            bed = new EmergencyBed(bedId, bedNumber, hospitalId, "AVAILABLE", floorWard, dailyCharge);
        } else {
            bed = new GeneralBed(bedId, bedNumber, hospitalId, "AVAILABLE", floorWard, dailyCharge);
        }
        return bedRepository.save(bed);
    }

    /**
     * Allocates a specific bed to a patient using pessimistic locking to prevent race conditions.
     */
    @Transactional
    public Bed allocateBed(String bedId, String patientId, String visitId) {
        Bed bed = bedRepository.findByIdForUpdate(bedId)
                .orElseThrow(() -> new IllegalArgumentException("Bed not found: " + bedId));

        if (!bed.checkAvailability()) {
            throw new IllegalStateException("Bed " + bed.getBedNumber() + " is already occupied or under maintenance.");
        }

        bed.allocate(patientId, visitId);
        return bedRepository.save(bed);
    }

    /**
     * Releases an occupied bed back into the available pool.
     */
    @Transactional
    public Bed releaseBed(String bedId) {
        Bed bed = bedRepository.findByIdForUpdate(bedId)
                .orElseThrow(() -> new IllegalArgumentException("Bed not found: " + bedId));

        bed.release();
        return bedRepository.save(bed);
    }
}
