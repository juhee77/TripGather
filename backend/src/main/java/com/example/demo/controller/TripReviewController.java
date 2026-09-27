package com.example.demo.controller;

import com.example.demo.dto.TripReviewResponse;
import com.example.demo.service.TripReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/reviews")
@RequiredArgsConstructor
public class TripReviewController {

    private final TripReviewService tripReviewService;

    @PostMapping
    public ResponseEntity<TripReviewResponse> createReview(
            @PathVariable Long tripId,
            @RequestBody com.example.demo.dto.TripReviewRequest request) {
        return ResponseEntity.ok(tripReviewService.createReview(
                tripId, request.getContent(), request.ratingOrDefault(),
                request.categoryOrDefault(), request.getImageUrls()));
    }

    @GetMapping
    public ResponseEntity<List<TripReviewResponse>> getReviews(
            @PathVariable Long tripId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(tripReviewService.getReviews(tripId, category));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long tripId, @PathVariable Long reviewId) {
        tripReviewService.deleteReview(tripId, reviewId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<TripReviewResponse> updateReview(
            @PathVariable Long tripId,
            @PathVariable Long reviewId,
            @RequestBody com.example.demo.dto.TripReviewRequest request) {
        return ResponseEntity.ok(tripReviewService.updateReview(
                reviewId, request.getContent(), request.ratingOrDefault(),
                request.categoryOrDefault(), request.getImageUrls()));
    }

    @GetMapping("/summary")
    public ResponseEntity<com.example.demo.dto.TripReviewSummaryResponse> getReviewSummary(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripReviewService.getReviewSummary(tripId));
    }
}
