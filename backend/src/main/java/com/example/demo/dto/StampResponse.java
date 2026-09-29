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

    /** 모임 스탬프의 대상 모임. 코스 완주 스탬프면 null. */
    private Long gatheringId;

    /**
     * 이 스탬프가 가리키는 회차 날짜.
     *
     * 정기편이면 몇 회차에 나왔는지를 화면에서 구분할 수 있다.
     * 회차 개념이 없는 스탬프(코스 완주, 미션)는 null 이다.
     */
    private java.time.LocalDate occurrenceDate;

    public static StampResponse from(Stamp stamp) {
        return StampResponse.builder()
                .missionId(stamp.getId())
                .missionTitle(stamp.getTitle())
                .stampImageUrl(stamp.getStampImageUrl())
                .completedAt(stamp.getCompletedAt())
                .gatheringId(stamp.getGatheringId())
                .occurrenceDate(stamp.getOccurrenceDate())
                .build();
    }
}
