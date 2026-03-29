package com.example.demo.dto;

import com.example.demo.domain.UserMissionStep;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StampResponse {
    private Long missionId;
    private String missionTitle;
    private String stampImageUrl;
    private LocalDateTime completedAt;

    public static StampResponse from(com.example.demo.domain.UserMission mission) {
        return StampResponse.builder()
                .missionId(mission.getId())
                .missionTitle(mission.getItinerary().getTitle())
                .stampImageUrl(mission.getStampImageUrl())
                .completedAt(mission.getCompletedAt())
                .build();
    }
}
