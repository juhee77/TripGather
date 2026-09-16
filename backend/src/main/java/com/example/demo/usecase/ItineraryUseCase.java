package com.example.demo.usecase;

import com.example.demo.domain.Itinerary;
import java.util.List;

public interface ItineraryUseCase {
    List<Itinerary> getAllItineraries();
    List<Itinerary> getPublicItineraries();

    /**
     * 열람 권한을 확인한 단건 조회.
     * 공개 여정은 누구나, 비공개 여정은 소유자 또는 해당 여정이 링크된 모임의 호스트/승인 크루만 볼 수 있다.
     */
    Itinerary getByIdForViewer(Long id);
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
