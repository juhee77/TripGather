package com.example.demo.controller;

import com.example.demo.dto.TripRequest;
import com.example.demo.dto.TripResponse;
import com.example.demo.dto.ItineraryResponse;
import com.example.demo.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@RequestBody TripRequest request) {
        return ResponseEntity.ok(tripService.createTrip(request));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getMyTrips() {
        return ResponseEntity.ok(tripService.getMyTrips());
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getTrip(tripId));
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable Long tripId, @RequestBody TripRequest request) {
        return ResponseEntity.ok(tripService.updateTrip(tripId, request));
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long tripId) {
        tripService.deleteTrip(tripId);
        return ResponseEntity.noContent().build();
    }

    // --- 일정 연결 ---

    @PostMapping("/{tripId}/itineraries/{itineraryId}")
    public ResponseEntity<Void> linkItinerary(@PathVariable Long tripId, @PathVariable Long itineraryId) {
        tripService.linkItinerary(tripId, itineraryId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{tripId}/itineraries/{itineraryId}")
    public ResponseEntity<Void> unlinkItinerary(@PathVariable Long tripId, @PathVariable Long itineraryId) {
        tripService.unlinkItinerary(tripId, itineraryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tripId}/itineraries")
    public ResponseEntity<List<ItineraryResponse>> getLinkedItineraries(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getLinkedItineraries(tripId));
    }

    // --- 추천 코스 ---

    @GetMapping("/{tripId}/recommendations")
    public ResponseEntity<List<ItineraryResponse>> getRecommendations(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getRecommendedItineraries(tripId));
    }
}
