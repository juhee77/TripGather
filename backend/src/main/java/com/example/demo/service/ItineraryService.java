package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;

    @Transactional(readOnly = true)
    public List<Itinerary> getAllItineraries() {
        return itineraryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Itinerary createItinerary(Itinerary itinerary) {
        if (itinerary.getRoutePoints() != null) {
            itinerary.getRoutePoints().forEach(rp -> rp.setItinerary(itinerary));
        }
        return itineraryRepository.save(itinerary);
    }
}
