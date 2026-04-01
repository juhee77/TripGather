package com.example.demo.usecase;

import com.example.demo.dto.UserMissionResponse;
import com.example.demo.dto.UserMissionStepResponse;
import com.example.demo.dto.StampResponse;
import java.util.List;

public interface UserMissionUseCase {
    UserMissionResponse startMission(Long itineraryId, String email);
    UserMissionResponse completeMission(Long missionId, String email);
    UserMissionStepResponse completeStep(Long missionId, Long stepId, String memo, String photoUrl, String email);
    List<UserMissionResponse> getMyMissions(String email);
    List<StampResponse> getMyStamps(String email);
    void requestLeave(Long missionId, String email);
    List<UserMissionResponse> getLeaveRequests(String hostEmail);
    void approveLeave(Long missionId, String hostEmail);
}
