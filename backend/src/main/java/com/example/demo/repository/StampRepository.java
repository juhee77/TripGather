package com.example.demo.repository;

import com.example.demo.domain.Stamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {
    List<Stamp> findByUserEmailOrderByCompletedAtDesc(String email);
    boolean existsByUserIdAndGatheringId(Long userId, Long gatheringId);
}

