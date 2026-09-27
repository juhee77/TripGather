package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.*;

/** 호스트가 미션을 출제하거나 수정할 때 보내는 본문. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringMissionRequest {
    @NotBlank(message = "미션 제목은 필수입니다.")
    @Size(max = 100, message = "미션 제목은 100자 이내여야 합니다.")
    private String title;
    @Size(max = 1000, message = "미션 설명은 1000자 이내여야 합니다.")
    private String description;
    /** null 이면 서비스가 기본 보상(50 PTS)을 적용한다. */
    @Min(value = 0, message = "보상 포인트는 0 이상이어야 합니다.")
    @Max(value = 10000, message = "보상 포인트는 10000을 넘을 수 없습니다.")
    private Integer rewardPoints;
    private Boolean requiresPhoto;
}
