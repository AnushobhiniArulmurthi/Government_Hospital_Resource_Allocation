package com.hospital.resourceallocation.controller;

import com.hospital.resourceallocation.dto.ApiResponse;
import com.hospital.resourceallocation.model.ConsultationWaitingQueueItem;
import com.hospital.resourceallocation.model.ResourceWaitingQueueItem;
import com.hospital.resourceallocation.service.WaitingQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Priority-Based Waiting Queues REST Controller
 */
@RestController
@RequestMapping("/api/queues")
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;

    @Autowired
    public WaitingQueueController(WaitingQueueService waitingQueueService) {
        this.waitingQueueService = waitingQueueService;
    }

    @GetMapping("/consultation")
    public ApiResponse<List<ConsultationWaitingQueueItem>> getConsultationQueue() {
        return ApiResponse.ok("Consultation queue ordered by severity and arrival time",
                waitingQueueService.getConsultationWaitingQueue());
    }

    @GetMapping("/resource")
    public ApiResponse<List<ResourceWaitingQueueItem>> getResourceQueue() {
        return ApiResponse.ok("Resource queue ordered by severity and arrival time",
                waitingQueueService.getResourceWaitingQueue());
    }

    @PostMapping("/consultation/process")
    public ApiResponse<String> processConsultationQueue() {
        waitingQueueService.processConsultationQueue();
        return ApiResponse.ok("Consultation queue re-evaluated against available doctors", "Processed successfully");
    }

    @PostMapping("/resource/process")
    public ApiResponse<String> processResourceQueue() {
        waitingQueueService.processResourceQueue();
        return ApiResponse.ok("Resource queue re-evaluated against available inventory and beds", "Processed successfully");
    }
}
