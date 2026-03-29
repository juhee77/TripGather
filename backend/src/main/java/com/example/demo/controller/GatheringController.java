package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.dto.GatheringResponse;
import com.example.demo.usecase.GatheringUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringUseCase gatheringService;

    @GetMapping
    public ResponseEntity<List<GatheringResponse>> getAllGatherings(@RequestParam(required = false) String location) {
        return ResponseEntity.ok(gatheringService.getAllGatherings(location).stream()
                .map(GatheringResponse::from)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/my/hosted")
    public ResponseEntity<List<GatheringResponse>> getMyHostedGatherings() {
        return ResponseEntity.ok(gatheringService.getHostedGatherings().stream()
                .map(GatheringResponse::from)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/my/joined")
    public ResponseEntity<List<GatheringResponse>> getMyJoinedGatherings() {
        return ResponseEntity.ok(gatheringService.getJoinedGatherings().stream()
                .map(GatheringResponse::from)
                .collect(java.util.stream.Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<GatheringResponse> createGathering(@RequestBody Gathering gathering) {
        return ResponseEntity.ok(GatheringResponse.from(gatheringService.createGathering(gathering)));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<GatheringResponse> joinGathering(@PathVariable Long id) {
        return ResponseEntity.ok(GatheringResponse.from(gatheringService.joinGathering(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GatheringResponse> updateGathering(@PathVariable Long id, @RequestBody Gathering gathering) {
        return ResponseEntity.ok(GatheringResponse.from(gatheringService.updateGathering(id, gathering)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGathering(@PathVariable Long id) {
        gatheringService.deleteGathering(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members/{userId}/approve")
    public ResponseEntity<Void> approveMember(@PathVariable Long id, @PathVariable Long userId) {
        gatheringService.approveMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members/{userId}/reject")
    public ResponseEntity<Void> rejectMember(@PathVariable Long id, @PathVariable Long userId) {
        gatheringService.rejectMember(id, userId);
        return ResponseEntity.ok().build();
    }
}
