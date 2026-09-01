package com.example.demo.controller;

import com.example.demo.dto.ItineraryResponse;
import com.example.demo.security.SecurityService;
import com.example.demo.usecase.ItineraryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 로그인한 사용자 본인의 여정만 다루는 엔드포인트.
 * 대상 사용자는 항상 인증 주체에서 얻으며, 클라이언트가 보낸 email 파라미터는 받지 않는다.
 */
@RestController
@RequestMapping("/api/my-trips")
@RequiredArgsConstructor
public class MyTripController {

    private final ItineraryUseCase itineraryService;
    private final SecurityService securityService;

    /**
     * Get all journeys owned by the current user.
     */
    @GetMapping
    public ResponseEntity<List<ItineraryResponse>> getMyJourneys() {
        return ResponseEntity.ok(itineraryService.getUserJourneys(securityService.getCurrentUserEmail()).stream()
                .map(ItineraryResponse::from)
                .toList());
    }

    /**
     * Clone an existing itinerary into the user's personal trip list.
     */
    @PostMapping("/clone")
    public ResponseEntity<ItineraryResponse> cloneItinerary(@RequestParam Long originalId) {
        return ResponseEntity.ok(ItineraryResponse.from(
                itineraryService.cloneItinerary(originalId, securityService.getCurrentUserEmail())));
    }

    /**
     * Toggle the visibility of a personal itinerary in the Travel Feed.
     */
    @PatchMapping("/{id}/share")
    public ResponseEntity<ItineraryResponse> togglePublic(@PathVariable Long id, @RequestParam boolean isPublic) {
        return ResponseEntity.ok(ItineraryResponse.from(
                itineraryService.togglePublicStatus(id, securityService.getCurrentUserEmail(), isPublic)));
    }

    /**
     * Merge points from another itinerary into an existing personal itinerary.
     */
    @PostMapping("/merge")
    public ResponseEntity<ItineraryResponse> mergeItinerary(@RequestParam Long sourceId,
                                                           @RequestParam Long targetId,
                                                           @RequestParam int targetDay) {
        return ResponseEntity.ok(ItineraryResponse.from(
                itineraryService.mergeItinerary(sourceId, targetId, targetDay, securityService.getCurrentUserEmail())));
    }
}
