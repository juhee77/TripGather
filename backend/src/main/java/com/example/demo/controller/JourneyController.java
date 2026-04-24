package com.example.demo.controller;

import com.example.demo.dto.ItineraryResponse;
import com.example.demo.usecase.JourneyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journeys")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyUseCase journeyUseCase;

    @GetMapping("/me/itineraries")
    public ResponseEntity<List<ItineraryResponse>> getMyJourneys(Authentication authentication) {
        return ResponseEntity.ok(
                journeyUseCase.getMyJourneys(authentication.getName())
                        .stream()
                        .map(ItineraryResponse::from)
                        .toList()
        );
    }

    @PostMapping("/me/itineraries/{itineraryId}")
    public ResponseEntity<ItineraryResponse> addToMyJourney(@PathVariable Long itineraryId, Authentication authentication) {
        return ResponseEntity.ok(ItineraryResponse.from(
                journeyUseCase.addToMyJourney(itineraryId, authentication.getName())
        ));
    }

    @DeleteMapping("/me/itineraries/{itineraryId}")
    public ResponseEntity<Void> removeFromMyJourney(@PathVariable Long itineraryId, Authentication authentication) {
        journeyUseCase.removeFromMyJourney(itineraryId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

