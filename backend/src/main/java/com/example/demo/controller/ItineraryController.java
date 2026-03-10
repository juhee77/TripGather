package com.example.demo.controller;

import com.example.demo.domain.Itinerary;
import com.example.demo.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ItineraryController {

    private final ItineraryService itineraryService;

    @GetMapping
    public ResponseEntity<List<Itinerary>> getAllItineraries() {
        return ResponseEntity.ok(itineraryService.getAllItineraries());
    }

    @PostMapping
    public ResponseEntity<Itinerary> createItinerary(@RequestBody Itinerary itinerary) {
        return ResponseEntity.ok(itineraryService.createItinerary(itinerary));
    }
}
