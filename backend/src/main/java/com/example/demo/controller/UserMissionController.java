package com.example.demo.controller;

import com.example.demo.dto.UserMissionResponse;
import com.example.demo.dto.UserMissionStepResponse;
import com.example.demo.usecase.UserMissionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class UserMissionController {

    private final UserMissionUseCase missionService;

    @PostMapping("/start/{itineraryId}")
    public ResponseEntity<UserMissionResponse> startMission(@PathVariable Long itineraryId, Authentication authentication) {
        UserMissionResponse res = missionService.startMission(itineraryId, authentication.getName());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/complete/{itineraryId}")
    public ResponseEntity<UserMissionResponse> completeMission(@PathVariable Long itineraryId, Authentication authentication) {
        UserMissionResponse res = missionService.completeMission(itineraryId, authentication.getName());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserMissionResponse>> getMyMissions(Authentication authentication) {
        List<UserMissionResponse> res = missionService.getMyMissions(authentication.getName());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/me/stamps")
    public ResponseEntity<List<com.example.demo.dto.StampResponse>> getMyStamps(Authentication authentication) {
        List<com.example.demo.dto.StampResponse> res = missionService.getMyStamps(authentication.getName());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{missionId}/steps/{stepId}/complete")
    public ResponseEntity<UserMissionStepResponse> completeStep(
            @PathVariable Long missionId,
            @PathVariable Long stepId,
            @RequestBody StepCompleteRequest request,
            Authentication authentication) {
        UserMissionStepResponse res = missionService.completeStep(missionId, stepId, request.getMemo(), request.getPhotoUrl(), authentication.getName());
        return ResponseEntity.ok(res);
    }

    @Data
    public static class StepCompleteRequest {
        private String memo;
        private String photoUrl;
    }
}
