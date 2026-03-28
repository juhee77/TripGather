package com.example.demo.dto;

import com.example.demo.domain.UserMission;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserMissionResponse {
    private Long id;
    private Long itineraryId;
    private String itineraryTitle;
    private String itineraryAuthor;
    private String itineraryLocation;
    private String itineraryDates;
    private String itineraryBgImageUrl;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private ItineraryResponse itinerary;
    private java.util.List<UserMissionStepResponse> steps;

    public static UserMissionResponse from(UserMission mission) {
        if (mission == null) return null;
        return UserMissionResponse.builder()
                .id(mission.getId())
                .itineraryId(mission.getItinerary().getId())
                .itineraryTitle(mission.getItinerary().getTitle())
                .itineraryAuthor(mission.getItinerary().getAuthor())
                .itineraryLocation(mission.getItinerary().getLocation())
                .itineraryDates(mission.getItinerary().getDates())
                .itineraryBgImageUrl(mission.getItinerary().getBgImageUrl())
                .status(mission.getStatus())
                .startedAt(mission.getStartedAt())
                .completedAt(mission.getCompletedAt())
                .itinerary(ItineraryResponse.from(mission.getItinerary()))
                .steps(mission.getSteps() != null ? 
                        mission.getSteps().stream()
                                .map(UserMissionStepResponse::from)
                                .toList() : null)
                .build();
    }
}
