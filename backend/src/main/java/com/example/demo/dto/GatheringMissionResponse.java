package com.example.demo.dto;

import com.example.demo.domain.GatheringMission;
import com.example.demo.domain.MissionCompletionStatus;
import lombok.*;

import java.util.List;

/**
 * 미션 한 건과, 그 미션을 보는 사람 기준의 상태를 함께 담는다.
 *
 * 화면에서 "이 미션을 내가 깼는지"와 "크루 중 몇 명이 깼는지"를 동시에 보여줘야 하는데
 * 이걸 프론트에서 조립하게 두면 목록 API 를 두 번 부르게 된다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringMissionResponse {

    private Long id;
    private Long gatheringId;
    private String title;
    private String description;
    private int rewardPoints;
    private boolean requiresPhoto;

    /** 이 미션을 승인받은 크루 수 */
    private int approvedCount;
    /** 호스트 심사를 기다리는 인증 수 */
    private int pendingCount;

    /** 요청한 사람의 인증 상태. 아직 올린 적이 없으면 null */
    private MissionCompletionStatus myStatus;
    /** 요청한 사람의 인증 기록 id. 없으면 null */
    private Long myCompletionId;
    private String myPhotoUrl;
    private String myMemo;

    /** 호스트가 볼 때만 채워지는 심사 대기 목록 */
    private List<MissionCompletionResponse> pendingCompletions;

    public static GatheringMissionResponse from(GatheringMission mission) {
        return GatheringMissionResponse.builder()
                .id(mission.getId())
                .gatheringId(mission.getGathering() != null ? mission.getGathering().getId() : null)
                .title(mission.getTitle())
                .description(mission.getDescription())
                .rewardPoints(mission.getRewardPoints())
                .requiresPhoto(mission.isRequiresPhoto())
                .build();
    }
}
