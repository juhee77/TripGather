package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.dto.TripRequest;
import com.example.demo.dto.TripResponse;
import com.example.demo.dto.ItineraryResponse;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.TripItineraryRepository;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripItineraryRepository tripItineraryRepository;
    private final ItineraryRepository itineraryRepository;
    private final SecurityService securityService;

    @Transactional
    public TripResponse createTrip(TripRequest request) {
        User owner = securityService.getCurrentUser();
        Trip trip = Trip.of(request.getTitle(), request.getDestination(), request.getCountry(), owner);
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setBgImageUrl(request.getBgImageUrl());
        if (request.getStatus() != null) {
            trip.setStatus(TripStatus.valueOf(request.getStatus()));
        }
        Trip saved = tripRepository.save(trip);
        return TripResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getMyTrips() {
        String email = securityService.getCurrentUserEmail();
        return tripRepository.findByOwnerEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(TripResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(Long tripId) {
        Trip trip = findTripById(tripId);
        return TripResponse.from(trip);
    }

    @Transactional
    public TripResponse updateTrip(Long tripId, TripRequest request) {
        Trip trip = findOwnedTrip(tripId);
        if (request.getTitle() != null) trip.setTitle(request.getTitle());
        if (request.getDestination() != null) trip.setDestination(request.getDestination());
        if (request.getCountry() != null) trip.setCountry(request.getCountry());
        if (request.getStartDate() != null) trip.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) trip.setEndDate(request.getEndDate());
        if (request.getBgImageUrl() != null) trip.setBgImageUrl(request.getBgImageUrl());
        if (request.getStatus() != null) trip.setStatus(TripStatus.valueOf(request.getStatus()));
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = findOwnedTrip(tripId);
        tripRepository.delete(trip);
    }

    // --- 일정 연결 ---

    @Transactional
    public void linkItinerary(Long tripId, Long itineraryId) {
        Trip trip = findOwnedTrip(tripId);
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "일정을 찾을 수 없습니다."));
        if (tripItineraryRepository.existsByTripIdAndItineraryId(tripId, itineraryId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 연결된 일정입니다.");
        }
        int nextOrder = trip.getTripItineraries().size();
        TripItinerary link = TripItinerary.of(trip, itinerary, nextOrder);
        tripItineraryRepository.save(link);
    }

    @Transactional
    public void unlinkItinerary(Long tripId, Long itineraryId) {
        findOwnedTrip(tripId);
        TripItinerary link = tripItineraryRepository.findByTripIdAndItineraryId(tripId, itineraryId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "연결된 일정을 찾을 수 없습니다."));
        tripItineraryRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public List<ItineraryResponse> getLinkedItineraries(Long tripId) {
        return tripItineraryRepository.findByTripIdOrderByDisplayOrder(tripId)
                .stream()
                .map(ti -> ItineraryResponse.from(ti.getItinerary()))
                .toList();
    }

    // --- 추천 코스 ---

    @Transactional(readOnly = true)
    public List<ItineraryResponse> getRecommendedItineraries(Long tripId) {
        Trip trip = findTripById(tripId);
        String destination = trip.getDestination();
        if (destination == null || destination.isBlank()) return List.of();

        return itineraryRepository.findAll().stream()
                .filter(it -> it.isPublicStatus()
                        && it.getLocation() != null
                        && it.getLocation().contains(destination))
                .map(ItineraryResponse::from)
                .toList();
    }

    // --- Helper ---

    private Trip findTripById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다."));
    }

    private Trip findOwnedTrip(Long tripId) {
        Trip trip = findTripById(tripId);
        String email = securityService.getCurrentUserEmail();
        if (!trip.getOwner().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "본인의 여행만 관리할 수 있습니다.");
        }
        return trip;
    }
}
