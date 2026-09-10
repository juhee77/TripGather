package com.example.demo.usecase;

import com.example.demo.dto.GatheringMissionRequest;
import com.example.demo.dto.GatheringMissionResponse;
import com.example.demo.dto.MissionCompletionResponse;
import com.example.demo.dto.MissionProgressResponse;
import com.example.demo.dto.MissionSubmitRequest;

import java.util.List;

public interface GatheringMissionUseCase {

    /* 호스트 */
    GatheringMissionResponse createMission(Long gatheringId, GatheringMissionRequest request);

    GatheringMissionResponse updateMission(Long gatheringId, Long missionId, GatheringMissionRequest request);

    void deleteMission(Long gatheringId, Long missionId);

    List<MissionCompletionResponse> getPendingCompletions(Long gatheringId);

    MissionCompletionResponse approveCompletion(Long gatheringId, Long completionId);

    MissionCompletionResponse rejectCompletion(Long gatheringId, Long completionId);

    /* 크루 */
    List<GatheringMissionResponse> getMissions(Long gatheringId);

    MissionCompletionResponse submitCompletion(Long gatheringId, Long missionId, MissionSubmitRequest request);

    MissionProgressResponse getMyProgress(Long gatheringId);
}
