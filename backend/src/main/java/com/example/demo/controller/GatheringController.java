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
}
