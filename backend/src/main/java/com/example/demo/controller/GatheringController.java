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

    @GetMapping("/{id}")
    public ResponseEntity<GatheringResponse> getGathering(@PathVariable Long id) {
        return ResponseEntity.ok(GatheringResponse.from(gatheringService.getGathering(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GatheringResponse>> searchGatherings(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean availableOnly) {
        return ResponseEntity.ok(gatheringService.searchGatherings(query, category, location, availableOnly).stream()
                .map(GatheringResponse::from)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/my/hosted")
    public ResponseEntity<List<GatheringResponse>> getMyHostedGatherings() {
        return ResponseEntity.ok(gatheringService.getHostedGatherings().stream()
                .map(GatheringResponse::from)
                .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping({"/my/joined", "/my/participating"})
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

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGathering(@PathVariable Long id) {
        gatheringService.leaveGathering(id);
        return ResponseEntity.ok().build();
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

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeGathering(@PathVariable Long id) {
        gatheringService.likeGathering(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/invite/{userId}")
    public ResponseEntity<Void> inviteMember(@PathVariable Long id, @PathVariable Long userId) {
        gatheringService.inviteMember(id, userId);
        return ResponseEntity.ok().build();
    }
}
