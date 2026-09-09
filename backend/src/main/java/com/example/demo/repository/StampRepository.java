package com.example.demo.repository;

import com.example.demo.domain.Stamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {
    List<Stamp> findByUserEmailOrderByCompletedAtDesc(String email);
    boolean existsByUserIdAndGatheringId(Long userId, Long gatheringId);
    long countByUserId(Long userId);

    /** 피드 렌더링용: 사용자가 스탬프를 받은 모임 ID 를 한 번에 읽는다. */
    @org.springframework.data.jpa.repository.Query(
            "SELECT s.gatheringId FROM Stamp s WHERE s.user.id = :userId AND s.gatheringId IS NOT NULL")
    java.util.List<Long> findStampedGatheringIdsByUserId(
            @org.springframework.data.repository.query.Param("userId") Long userId);
}

