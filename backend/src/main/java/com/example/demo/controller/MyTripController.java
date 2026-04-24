package com.example.demo.controller;

import com.example.demo.dto.ItineraryResponse;
import com.example.demo.usecase.ItineraryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-trips")
@RequiredArgsConstructor
public class MyTripController {

    private final ItineraryUseCase itineraryService;

    /**
     * Get all journeys owned by the current user.
     */
    @GetMapping
    public ResponseEntity<List<ItineraryResponse>> getMyJourneys(@RequestParam String email) {
        return ResponseEntity.ok(itineraryService.getUserJourneys(email).stream()
                .map(ItineraryResponse::from)
                .toList());
    }

    /**
     * Clone an existing itinerary into the user's personal trip list.
     */
    @PostMapping("/clone")
    public ResponseEntity<ItineraryResponse> cloneItinerary(@RequestParam Long originalId, @RequestParam String email) {
        return ResponseEntity.ok(ItineraryResponse.from(itineraryService.cloneItinerary(originalId, email)));
    }

    /**
     * Toggle the visibility of a personal itinerary in the Travel Feed.
     */
    @PatchMapping("/{id}/share")
    public ResponseEntity<ItineraryResponse> togglePublic(@PathVariable Long id, @RequestParam String email, @RequestParam boolean isPublic) {
        return ResponseEntity.ok(ItineraryResponse.from(itineraryService.togglePublicStatus(id, email, isPublic)));
    }
}
