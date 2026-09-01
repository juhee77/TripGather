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
    Itinerary mergeItinerary(Long sourceId, Long targetId, int targetDay, String requesterEmail);
    com.example.demo.domain.RoutePoint toggleRoutePointCompletion(Long itineraryId, Long pointId, String userEmail);
    void deleteItinerary(Long id);
}
