package com.hospital.resourceallocation.repository;

import com.hospital.resourceallocation.model.ResourceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceHistoryRepository extends JpaRepository<ResourceHistory, Integer> {
    List<ResourceHistory> findAllByOrderByTimestampDesc();
    List<ResourceHistory> findByResourceIdOrderByTimestampDesc(String resourceId);
    List<ResourceHistory> findByVisitIdOrderByTimestampDesc(String visitId);
}
