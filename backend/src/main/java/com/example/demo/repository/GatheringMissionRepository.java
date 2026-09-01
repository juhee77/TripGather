package com.example.demo.repository;

import com.example.demo.domain.GatheringMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GatheringMissionRepository extends JpaRepository<GatheringMission, Long> {

    List<GatheringMission> findByGatheringIdOrderByCreatedAtAsc(Long gatheringId);

    long countByGatheringId(Long gatheringId);
}
