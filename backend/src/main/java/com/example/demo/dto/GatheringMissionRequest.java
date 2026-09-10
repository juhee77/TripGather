package com.example.demo.dto;

import lombok.*;

/** 호스트가 미션을 출제하거나 수정할 때 보내는 본문. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringMissionRequest {
    private String title;
    private String description;
    /** null 이면 서비스가 기본 보상(50 PTS)을 적용한다. */
    private Integer rewardPoints;
    private Boolean requiresPhoto;
}
