package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.model.Bed;
import com.hospital.resourceallocation.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Bed Management REST Controller
 */
@RestController
@RequestMapping("/api/beds")
public class BedController {

    private final BedService bedService;

    @Autowired
    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping
    public ApiResponse<List<Bed>> getAllBeds(@RequestParam(defaultValue = "H-001") String hospitalId,
                                            @RequestParam(required = false) String bedType) {
        if (bedType != null && !bedType.trim().isEmpty()) {
            return ApiResponse.ok(bedService.getAvailableBedsByType(hospitalId, bedType));
        }
        return ApiResponse.ok(bedService.getBedsByHospital(hospitalId));
    }

    @GetMapping("/available")
    public ApiResponse<List<Bed>> getAvailableBeds(@RequestParam(defaultValue = "H-001") String hospitalId) {
        return ApiResponse.ok(bedService.getAvailableBeds(hospitalId));
    }

    @GetMapping("/occupied")
    public ApiResponse<List<Bed>> getOccupiedBeds(@RequestParam(defaultValue = "H-001") String hospitalId) {
        return ApiResponse.ok(bedService.getOccupiedBeds(hospitalId));
    }

    @PostMapping
    public ApiResponse<Bed> addBed(@RequestBody Map<String, Object> body) {
        String bedId = (String) body.get("bedId");
        String bedNumber = (String) body.get("bedNumber");
        String bedType = (String) body.get("bedType");
        String hospitalId = body.get("hospitalId") != null ? (String) body.get("hospitalId") : "H-001";
        String floorWard = (String) body.get("floorWard");
        Double dailyCharge = body.get("dailyCharge") != null ? Double.parseDouble(body.get("dailyCharge").toString()) : 0.0;

        Bed created = bedService.addBed(bedId, bedNumber, bedType, hospitalId, floorWard, dailyCharge);
        return ApiResponse.ok("Bed added successfully", created);
    }

    @PostMapping("/allocate")
    public ApiResponse<Bed> allocateBed(@RequestBody Map<String, String> body) {
        String bedId = body.get("bedId");
        String patientId = body.get("patientId");
        String visitId = body.get("visitId");
        Bed allocated = bedService.allocateBed(bedId, patientId, visitId);
        return ApiResponse.ok("Bed " + allocated.getBedNumber() + " allocated successfully", allocated);
    }

    @PostMapping("/release/{bedId}")
    public ApiResponse<Bed> releaseBed(@PathVariable String bedId) {
        Bed released = bedService.releaseBed(bedId);
        return ApiResponse.ok("Bed " + released.getBedNumber() + " released successfully", released);
    }
}
