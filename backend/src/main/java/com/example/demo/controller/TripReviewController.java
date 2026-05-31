package com.example.demo.controller;

import com.example.demo.dto.TripReviewResponse;
import com.example.demo.service.TripReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips/{tripId}/reviews")
@RequiredArgsConstructor
public class TripReviewController {

    private final TripReviewService tripReviewService;

    @PostMapping
    public ResponseEntity<TripReviewResponse> createReview(
            @PathVariable Long tripId,
            @RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        int rating = body.get("rating") != null ? ((Number) body.get("rating")).intValue() : 5;
        String category = (String) body.getOrDefault("category", "관광지");
        String imageUrls = (String) body.get("imageUrls");
        return ResponseEntity.ok(tripReviewService.createReview(tripId, content, rating, category, imageUrls));
    }

    @GetMapping
    public ResponseEntity<List<TripReviewResponse>> getReviews(
            @PathVariable Long tripId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(tripReviewService.getReviews(tripId, category));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long tripId, @PathVariable Long reviewId) {
        tripReviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
