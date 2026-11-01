package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.ResourceWaitingQueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceQueueRepository extends JpaRepository<ResourceWaitingQueueItem, String> {
    List<ResourceWaitingQueueItem> findByStatusOrderByPriorityScoreAscArrivalTimeAsc(String status);
    List<ResourceWaitingQueueItem> findByVisitId(String visitId);
}
