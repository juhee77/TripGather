package com.example.demo.controller;

import com.example.demo.domain.Itinerary;
import com.example.demo.dto.ItineraryResponse;
import com.example.demo.usecase.ItineraryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryUseCase itineraryService;

    @GetMapping
    public ResponseEntity<List<ItineraryResponse>> getAllItineraries() {
        return ResponseEntity.ok(itineraryService.getAllItineraries().stream()
                .map(ItineraryResponse::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItineraryResponse> getItinerary(@PathVariable Long id) {
        return ResponseEntity.ok(ItineraryResponse.from(itineraryService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ItineraryResponse> createItinerary(@RequestBody Itinerary itinerary) {
        return ResponseEntity.ok(ItineraryResponse.from(itineraryService.createItinerary(itinerary)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItineraryResponse> updateItinerary(@PathVariable Long id, @RequestBody Itinerary update) {
        return ResponseEntity.ok(ItineraryResponse.from(itineraryService.updateItinerary(id, update)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Long id) {
        itineraryService.deleteItinerary(id);
        return ResponseEntity.noContent().build();
    }
}
