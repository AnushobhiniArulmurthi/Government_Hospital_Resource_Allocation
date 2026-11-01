package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.dto.DashboardStatsDTO;
import com.hospital.resourceallocation.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main Application Dashboard REST Controller
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDTO> getDashboardStats() {
        return ApiResponse.ok("Dashboard statistics retrieved successfully", dashboardService.getDashboardStats());
    }
}
