package com.example.demo.dto;

import com.example.demo.domain.Stamp;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StampResponse {
    private Long missionId;
    private String missionTitle;
    private String stampImageUrl;
    private LocalDateTime completedAt;

    public static StampResponse from(Stamp stamp) {
        return StampResponse.builder()
                .missionId(stamp.getId())
                .missionTitle(stamp.getTitle())
                .stampImageUrl(stamp.getStampImageUrl())
                .completedAt(stamp.getCompletedAt())
                .build();
    }
}
