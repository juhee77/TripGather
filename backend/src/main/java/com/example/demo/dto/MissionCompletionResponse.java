package com.example.demo.dto;

import com.example.demo.domain.MissionCompletion;
import com.example.demo.domain.MissionCompletionStatus;
import lombok.*;

import java.time.LocalDateTime;

/** 크루 한 명의 인증 기록. 호스트의 심사 목록과 미션 상세에서 함께 쓴다. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionCompletionResponse {

    private Long id;
    private Long missionId;
    private String missionTitle;
    private Long userId;
    private String userName;
    private String userEmail;
    private String photoUrl;
    private String memo;
    private MissionCompletionStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public static MissionCompletionResponse from(MissionCompletion completion) {
        return MissionCompletionResponse.builder()
                .id(completion.getId())
                .missionId(completion.getMission() != null ? completion.getMission().getId() : null)
                .missionTitle(completion.getMission() != null ? completion.getMission().getTitle() : null)
                .userId(completion.getUser() != null ? completion.getUser().getId() : null)
                .userName(completion.getUser() != null ? completion.getUser().getName() : null)
                .userEmail(completion.getUser() != null ? completion.getUser().getEmail() : null)
                .photoUrl(completion.getPhotoUrl())
                .memo(completion.getMemo())
                .status(completion.getStatus())
                .submittedAt(completion.getCreatedAt())
                .reviewedAt(completion.getReviewedAt())
                .build();
    }
}
