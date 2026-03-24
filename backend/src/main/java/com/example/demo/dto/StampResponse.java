package com.example.demo.dto;

import com.example.demo.domain.UserMissionStep;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StampResponse {
    private Long stepId;
    private Long missionId;
    private String missionTitle;
    private String dayLabel;
    private String routePointLabel;
    private String memo;
    private String photoUrl;
    private LocalDateTime completedAt;

    public static StampResponse from(UserMissionStep step) {
        return StampResponse.builder()
                .stepId(step.getId())
                .missionId(step.getUserMission().getId())
                .missionTitle(step.getUserMission().getItinerary().getTitle())
                .dayLabel(step.getRoutePoint().getDayLabel())
                .routePointLabel(step.getRoutePoint().getLabel())
                .memo(step.getMemo())
                .photoUrl(step.getPhotoUrl())
                .completedAt(step.getCompletedAt())
                .build();
    }
}
