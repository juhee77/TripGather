package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.domain.User;
import com.example.demo.domain.UserMission;
import com.example.demo.dto.UserMissionResponse;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.UserMissionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMissionService {

    private final UserMissionRepository missionRepository;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;

    @Transactional
    public UserMissionResponse startMission(Long itineraryId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found: " + itineraryId));

        // 이미 미션이 존재하는지 확인
        return missionRepository.findByUserIdAndItineraryId(user.getId(), itineraryId)
                .map(UserMissionResponse::from)
                .orElseGet(() -> {
                    UserMission mission = UserMission.builder()
                            .user(user)
                            .itinerary(itinerary)
                            .status("ACTIVE")
                            .startedAt(LocalDateTime.now())
                            .build();
                    return UserMissionResponse.from(missionRepository.save(mission));
                });
    }

    @Transactional
    public UserMissionResponse completeMission(Long itineraryId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        UserMission mission = missionRepository.findByUserIdAndItineraryId(user.getId(), itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Mission is not active for this itinerary."));

        if (!"COMPLETED".equals(mission.getStatus())) {
            mission.setStatus("COMPLETED");
            mission.setCompletedAt(LocalDateTime.now());
            missionRepository.save(mission);
        }

        return UserMissionResponse.from(mission);
    }

    @Transactional(readOnly = true)
    public List<UserMissionResponse> getMyMissions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return missionRepository.findByUserId(user.getId()).stream()
                .map(UserMissionResponse::from)
                .toList();
    }
}
