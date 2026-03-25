package com.example.demo.usecase;

import com.example.demo.domain.Itinerary;
import java.util.List;

public interface ItineraryUseCase {
    List<Itinerary> getAllItineraries();
    Itinerary getById(Long id);
    Itinerary createItinerary(Itinerary itinerary);
    Itinerary updateItinerary(Long id, Itinerary update);
    void deleteItinerary(Long id);
}
