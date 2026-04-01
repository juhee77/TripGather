package com.example.demo.repository;

import com.example.demo.domain.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT m FROM UserMission m WHERE m.itinerary.author = :hostName AND m.status = 'LEAVE_REQUESTED'")
    List<UserMission> findLeaveRequestsByHost(String hostName);

    Optional<UserMission> findByUserIdAndItineraryId(Long userId, Long itineraryId);
}
