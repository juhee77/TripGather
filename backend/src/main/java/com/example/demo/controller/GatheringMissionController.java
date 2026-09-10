package com.example.demo.controller;

import com.example.demo.dto.GatheringMissionRequest;
import com.example.demo.dto.GatheringMissionResponse;
import com.example.demo.dto.MissionCompletionResponse;
import com.example.demo.dto.MissionProgressResponse;
import com.example.demo.dto.MissionSubmitRequest;
import com.example.demo.usecase.GatheringMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/missions")
@RequiredArgsConstructor
public class GatheringMissionController {

    private final GatheringMissionUseCase missionService;

    /* 크루 */

    @GetMapping
    public ResponseEntity<List<GatheringMissionResponse>> getMissions(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(missionService.getMissions(gatheringId));
    }

    @GetMapping("/progress")
    public ResponseEntity<MissionProgressResponse> getMyProgress(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(missionService.getMyProgress(gatheringId));
    }

    @PostMapping("/{missionId}/submit")
    public ResponseEntity<MissionCompletionResponse> submit(
            @PathVariable Long gatheringId,
            @PathVariable Long missionId,
            @RequestBody(required = false) MissionSubmitRequest request) {
        return ResponseEntity.ok(missionService.submitCompletion(gatheringId, missionId, request));
    }

    /* 호스트 */

    @PostMapping
    public ResponseEntity<GatheringMissionResponse> create(
            @PathVariable Long gatheringId,
            @RequestBody GatheringMissionRequest request) {
        return ResponseEntity.ok(missionService.createMission(gatheringId, request));
    }

    @PutMapping("/{missionId}")
    public ResponseEntity<GatheringMissionResponse> update(
            @PathVariable Long gatheringId,
            @PathVariable Long missionId,
            @RequestBody GatheringMissionRequest request) {
        return ResponseEntity.ok(missionService.updateMission(gatheringId, missionId, request));
    }

    @DeleteMapping("/{missionId}")
    public ResponseEntity<Void> delete(@PathVariable Long gatheringId, @PathVariable Long missionId) {
        missionService.deleteMission(gatheringId, missionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/completions/pending")
    public ResponseEntity<List<MissionCompletionResponse>> getPending(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(missionService.getPendingCompletions(gatheringId));
    }

    @PatchMapping("/completions/{completionId}/approve")
    public ResponseEntity<MissionCompletionResponse> approve(
            @PathVariable Long gatheringId, @PathVariable Long completionId) {
        return ResponseEntity.ok(missionService.approveCompletion(gatheringId, completionId));
    }

    @PatchMapping("/completions/{completionId}/reject")
    public ResponseEntity<MissionCompletionResponse> reject(
            @PathVariable Long gatheringId, @PathVariable Long completionId) {
        return ResponseEntity.ok(missionService.rejectCompletion(gatheringId, completionId));
    }
}
