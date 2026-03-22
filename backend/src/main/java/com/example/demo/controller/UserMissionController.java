package com.example.demo.controller;

import com.example.demo.dto.UserMissionResponse;
import com.example.demo.service.UserMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserMissionController {

    private final UserMissionService missionService;

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
}
