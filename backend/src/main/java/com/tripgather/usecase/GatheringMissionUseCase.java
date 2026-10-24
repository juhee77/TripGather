package com.tripgather.usecase;

import com.tripgather.dto.GatheringMissionRequest;
import com.tripgather.dto.GatheringMissionResponse;
import com.tripgather.dto.MissionCompletionResponse;
import com.tripgather.dto.MissionProgressResponse;
import com.tripgather.dto.MissionSubmitRequest;

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
