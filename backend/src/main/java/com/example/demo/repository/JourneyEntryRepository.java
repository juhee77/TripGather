package com.example.demo.repository;

import com.example.demo.domain.JourneyEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JourneyEntryRepository extends JpaRepository<JourneyEntry, Long> {
    List<JourneyEntry> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<JourneyEntry> findByUserIdAndItineraryId(Long userId, Long itineraryId);
}

