package com.example.demo.repository;

import com.example.demo.domain.UserMissionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMissionStepRepository extends JpaRepository<UserMissionStep, Long> {
    List<UserMissionStep> findByUserMissionId(Long userMissionId);
}
