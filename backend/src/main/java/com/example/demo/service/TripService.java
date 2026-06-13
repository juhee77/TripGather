package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.dto.TripRequest;
import com.example.demo.dto.TripResponse;
import com.example.demo.dto.ItineraryResponse;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.TripRepository;
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
    private final ItineraryRepository itineraryRepository;
    private final SecurityService securityService;
    private final PackingService packingService; // 자동 템플릿 세팅을 위한 서비스 주입

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

        // 1:1 일정 설정 (복제된 일정 ID가 전달된 경우 해당 일정 바인딩, 없으면 새 빈 일정 생성)
        if (request.getItineraryId() != null) {
            Itinerary existing = itineraryRepository.findById(request.getItineraryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "지정된 일정을 찾을 수 없습니다."));
            existing.setOwnerEmail(owner.getEmail());
            existing.setStartDate(request.getStartDate());
            existing.setEndDate(request.getEndDate());
            existing.setLocation(request.getDestination());
            trip.setItinerary(existing);
        } else {
            Itinerary blankItinerary = Itinerary.builder()
                    .title(request.getTitle() + " 일정표")
                    .description(request.getTitle() + " 여행의 고유 일정표입니다.")
                    .location(request.getDestination())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .ownerEmail(owner.getEmail())
                    .author(owner.getName())
                    .authorEmail(owner.getEmail())
                    .publicStatus(false)
                    .routePoints(new java.util.ArrayList<>())
                    .build();
            Itinerary savedItinerary = itineraryRepository.save(blankItinerary);
            trip.setItinerary(savedItinerary);
        }

        Trip saved = tripRepository.save(trip);
        
        // 텅 빈 화면(Empty State) 이탈 방지: 기본 필수 준비물 세트 자동 세팅
        packingService.initDefaultItems(saved.getId());
        
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
        if (request.getTitle() != null) {
            trip.setTitle(request.getTitle());
            if (trip.getItinerary() != null) {
                trip.getItinerary().setTitle(request.getTitle() + " 일정표");
            }
        }
        if (request.getDestination() != null) {
            trip.setDestination(request.getDestination());
            if (trip.getItinerary() != null) {
                trip.getItinerary().setLocation(request.getDestination());
            }
        }
        if (request.getCountry() != null) {
            trip.setCountry(request.getCountry());
        }
        if (request.getStartDate() != null) {
            trip.setStartDate(request.getStartDate());
            if (trip.getItinerary() != null) {
                trip.getItinerary().setStartDate(request.getStartDate());
            }
        }
        if (request.getEndDate() != null) {
            trip.setEndDate(request.getEndDate());
            if (trip.getItinerary() != null) {
                trip.getItinerary().setEndDate(request.getEndDate());
            }
        }
        if (request.getBgImageUrl() != null) trip.setBgImageUrl(request.getBgImageUrl());
        if (request.getStatus() != null) trip.setStatus(TripStatus.valueOf(request.getStatus()));
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = findOwnedTrip(tripId);
        tripRepository.delete(trip);
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
