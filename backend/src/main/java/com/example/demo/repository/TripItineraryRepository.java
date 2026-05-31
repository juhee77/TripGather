package com.example.demo.repository;

import com.example.demo.domain.TripItinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripItineraryRepository extends JpaRepository<TripItinerary, Long> {
    List<TripItinerary> findByTripIdOrderByDisplayOrder(Long tripId);
    Optional<TripItinerary> findByTripIdAndItineraryId(Long tripId, Long itineraryId);
    boolean existsByTripIdAndItineraryId(Long tripId, Long itineraryId);
}
