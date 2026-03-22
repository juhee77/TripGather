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
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static UserMissionResponse from(UserMission mission) {
        if (mission == null) return null;
        return UserMissionResponse.builder()
                .id(mission.getId())
                .itineraryId(mission.getItinerary().getId())
                .itineraryTitle(mission.getItinerary().getTitle())
                .itineraryAuthor(mission.getItinerary().getAuthor())
                .status(mission.getStatus())
                .startedAt(mission.getStartedAt())
                .completedAt(mission.getCompletedAt())
                .build();
    }
}
