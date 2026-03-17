package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.service.GatheringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend dev server
public class GatheringController {

    private final GatheringService gatheringService;

    @GetMapping
    public ResponseEntity<List<Gathering>> getAllGatherings() {
        return ResponseEntity.ok(gatheringService.getAllGatherings());
    }

    @PostMapping
    public ResponseEntity<Gathering> createGathering(@RequestBody Gathering gathering) {
        return ResponseEntity.ok(gatheringService.createGathering(gathering));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Gathering> joinGathering(@PathVariable Long id) {
        return ResponseEntity.ok(gatheringService.joinGathering(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gathering> updateGathering(@PathVariable Long id, @RequestBody Gathering gathering) {
        return ResponseEntity.ok(gatheringService.updateGathering(id, gathering));
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
