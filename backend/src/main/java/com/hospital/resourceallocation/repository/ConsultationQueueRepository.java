package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.ConsultationWaitingQueueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationQueueRepository extends JpaRepository<ConsultationWaitingQueueItem, String> {
    List<ConsultationWaitingQueueItem> findByStatusOrderByPriorityScoreAscArrivalTimeAsc(String status);
    List<ConsultationWaitingQueueItem> findByVisitId(String visitId);
}
