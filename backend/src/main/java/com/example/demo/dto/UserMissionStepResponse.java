package com.example.demo.dto;

import com.example.demo.domain.UserMissionStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMissionStepResponse {
    private Long id;
    private Long routePointId;
    private String label;
    private int dayNumber;
    private int sequenceOrder;
    private boolean isCompleted;
    private String memo;
    private String photoUrl;
    private LocalDateTime completedAt;

    public static UserMissionStepResponse from(UserMissionStep step) {
        if (step == null) return null;
        return UserMissionStepResponse.builder()
                .id(step.getId())
                .routePointId(step.getRoutePoint() != null ? step.getRoutePoint().getId() : null)
                .label(step.getRoutePoint() != null ? step.getRoutePoint().getLabel() : "")
                .dayNumber(step.getRoutePoint() != null ? step.getRoutePoint().getDayNumber() : 1)
                .sequenceOrder(step.getRoutePoint() != null ? step.getRoutePoint().getSequenceOrder() : 0)
                .isCompleted(step.isCompleted())
                .memo(step.getMemo())
                .photoUrl(step.getPhotoUrl())
                .completedAt(step.getCompletedAt())
                .build();
    }
}
