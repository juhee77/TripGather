package com.example.demo.dto;

import lombok.*;

/** 모임 미션 전체에 대한 내 진행도. 탭 헤더의 요약 배지에 쓴다. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionProgressResponse {
    private Long gatheringId;
    private int totalCount;
    private int clearedCount;
    private double progressPercentage;
    /** 승인된 미션으로 지금까지 받은 포인트 합계 */
    private int earnedPoints;
}
