package com.example.demo.usecase;

import com.example.demo.domain.Itinerary;
import java.util.List;

public interface ItineraryUseCase {
    List<Itinerary> getAllItineraries();
    List<Itinerary> getPublicItineraries();
    List<Itinerary> getUserJourneys(String email);
    Itinerary getById(Long id);
    Itinerary createItinerary(Itinerary itinerary);
    Itinerary updateItinerary(Long id, Itinerary update);
    Itinerary cloneItinerary(Long originalId, String ownerEmail);
    Itinerary togglePublicStatus(Long id, String email, boolean isPublic);
    void deleteItinerary(Long id);
}
