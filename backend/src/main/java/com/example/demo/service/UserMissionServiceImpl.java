package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.domain.User;
import com.example.demo.domain.UserMission;
import com.example.demo.dto.UserMissionResponse;
import com.example.demo.dto.StampResponse;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.UserMissionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.example.demo.repository.UserMissionStepRepository;
import com.example.demo.dto.UserMissionStepResponse;
import com.example.demo.domain.UserMissionStep;
import com.example.demo.usecase.UserMissionUseCase;
import com.example.demo.service.FileService;

@Service
@RequiredArgsConstructor
public class UserMissionServiceImpl implements UserMissionUseCase {

    private final UserMissionRepository missionRepository;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final UserMissionStepRepository stepRepository;
    private final FileService fileService;

    @Transactional
    public UserMissionResponse startMission(Long itineraryId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found: " + itineraryId));

        // 이미 미션이 존재하는지 확인
        return missionRepository.findByUserIdAndItineraryId(user.getId(), itineraryId)
                .map(mission -> {
                    UserMissionResponse res = UserMissionResponse.from(mission);
                    res.setSteps(stepRepository.findByUserMissionId(mission.getId()).stream()
                            .map(UserMissionStepResponse::from).collect(Collectors.toList()));
                    return res;
                })
                .orElseGet(() -> {
                    UserMission mission = UserMission.builder()
                            .user(user)
                            .itinerary(itinerary)
                            .status("ACTIVE")
                            .startedAt(LocalDateTime.now())
                            .stampImageUrl(itinerary.getStampImageUrl()) // Inherit stamp image if pre-defined
                            .build();
                    UserMission savedMission = missionRepository.save(mission);
                    
                    List<UserMissionStep> steps = itinerary.getRoutePoints().stream().map(rp -> 
                        UserMissionStep.builder()
                            .userMission(savedMission)
                            .routePoint(rp)
                            .isCompleted(false)
                            .build()
                    ).collect(Collectors.toList());
                    stepRepository.saveAll(steps);
                    
                    UserMissionResponse res = UserMissionResponse.from(savedMission);
                    res.setSteps(steps.stream().map(UserMissionStepResponse::from).collect(Collectors.toList()));
                    return res;
                });
    }

    @Transactional
    public UserMissionResponse completeMission(Long missionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        UserMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        if (!mission.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not your mission");
        }

        if (!"COMPLETED".equals(mission.getStatus())) {
            mission.setStatus("COMPLETED");
            mission.setCompletedAt(LocalDateTime.now());
            
            // Auto-generate stamp image if not exists and not already inherited/set
            if (mission.getStampImageUrl() == null || mission.getStampImageUrl().isEmpty()) {
                String seed = mission.getItinerary().getTitle().replaceAll("\\s+", "_");
                mission.setStampImageUrl("https://api.dicebear.com/7.x/bottts/svg?seed=" + seed + "&backgroundColor=ffd43b,ff922b");
            }
            
            missionRepository.save(mission);
        }

        UserMissionResponse res = UserMissionResponse.from(mission);
        res.setSteps(stepRepository.findByUserMissionId(mission.getId()).stream()
                .map(UserMissionStepResponse::from).collect(Collectors.toList()));
        return res;
    }

    @Transactional
    public UserMissionStepResponse completeStep(Long missionId, Long stepId, String memo, String photoUrl, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        UserMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));

        if (!mission.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not your mission");
        }

        UserMissionStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step not found"));

        if (!step.getUserMission().getId().equals(mission.getId())) {
            throw new IllegalArgumentException("Step does not belong to this mission");
        }

        // 기존 사진이 있고 새로운 사진으로 교체되는 경우 기존 사진 삭제
        if (step.getPhotoUrl() != null && !step.getPhotoUrl().equals(photoUrl)) {
            try {
                fileService.deleteFile(step.getPhotoUrl());
            } catch (Exception e) {
                // 삭제 실패는 비즈니스 로직에 영향을 주지 않도록 로그만 남기고 진행 (필요시 로그 라이브러리 사용)
                System.err.println("기존 파일 삭제 실패: " + e.getMessage());
            }
        }

        step.setCompleted(true);
        step.setMemo(memo);
        step.setPhotoUrl(photoUrl);
        step.setCompletedAt(LocalDateTime.now());
        
        return UserMissionStepResponse.from(stepRepository.save(step));
    }

    @Transactional(readOnly = true)
    public List<UserMissionResponse> getMyMissions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return missionRepository.findByUserId(user.getId()).stream()
                .map(mission -> {
                    UserMissionResponse res = UserMissionResponse.from(mission);
                    res.setSteps(stepRepository.findByUserMissionId(mission.getId()).stream()
                            .map(UserMissionStepResponse::from).collect(Collectors.toList()));
                    return res;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StampResponse> getMyStamps(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return missionRepository.findByUserId(user.getId()).stream()
                .filter(m -> "COMPLETED".equals(m.getStatus()))
                .map(StampResponse::from)
                .toList();
    }

    @Transactional
    public void requestLeave(Long missionId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        
        if (!mission.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 챌린지만 중단 요청을 할 수 있습니다.");
        }
        
        mission.setStatus("LEAVE_REQUESTED");
        missionRepository.save(mission);
    }

    @Transactional(readOnly = true)
    public List<UserMissionResponse> getLeaveRequests(String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new IllegalArgumentException("Host not found"));
        
        return missionRepository.findLeaveRequestsByHost(host.getName()).stream()
                .map(mission -> {
                    UserMissionResponse res = UserMissionResponse.from(mission);
                    res.setSteps(stepRepository.findByUserMissionId(mission.getId()).stream()
                            .map(UserMissionStepResponse::from).collect(Collectors.toList()));
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveLeave(Long missionId, String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new IllegalArgumentException("Host not found"));
        
        UserMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        
        // Itinerary.author가 작성자의 이름(Name)이라고 가정
        if (!mission.getItinerary().getAuthor().equals(host.getName())) {
            throw new IllegalStateException("해당 일정의 작성자만 승인할 수 있습니다.");
        }

        // 미션 삭제 전 연관된 모든 단계의 사진 삭제
        List<UserMissionStep> steps = stepRepository.findByUserMissionId(mission.getId());
        for (UserMissionStep step : steps) {
            if (step.getPhotoUrl() != null && !step.getPhotoUrl().isEmpty()) {
                try {
                    fileService.deleteFile(step.getPhotoUrl());
                } catch (Exception e) {
                    System.err.println("미션 삭제 중 파일 삭제 실패: " + e.getMessage());
                }
            }
        }
        
        missionRepository.delete(mission);
    }
}
