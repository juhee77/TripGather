package com.example.demo.usecase;

import com.example.demo.domain.Itinerary;

import java.util.List;

public interface JourneyUseCase {
    List<Itinerary> getMyJourneys(String email);
    Itinerary addToMyJourney(Long itineraryId, String email);
    void removeFromMyJourney(Long itineraryId, String email);
}

