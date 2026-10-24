package com.tripgather.repository;

import com.tripgather.domain.TripReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripReviewRepository extends JpaRepository<TripReview, Long> {
    List<TripReview> findByTripIdOrderByCreatedAtDesc(Long tripId);
    List<TripReview> findByTripIdAndCategoryOrderByCreatedAtDesc(Long tripId, String category);
}
