package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.GatheringMission;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.MissionCompletion;
import com.example.demo.domain.MissionCompletionStatus;
import com.example.demo.domain.User;
import com.example.demo.dto.GatheringMissionRequest;
import com.example.demo.dto.GatheringMissionResponse;
import com.example.demo.dto.MissionCompletionResponse;
import com.example.demo.dto.MissionProgressResponse;
import com.example.demo.dto.MissionSubmitRequest;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.GatheringMissionRepository;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.MissionCompletionRepository;
import com.example.demo.security.SecurityService;
import com.example.demo.usecase.GatheringMissionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 모임 미션의 출제와 인증 심사를 담당한다.
 *
 * 보상 지급은 이 서비스가 직접 하지 않고 PointService 에 위임한다.
 * 포인트 적립과 스탬프 발급이 거기서 비관적 락으로 함께 처리되고 있어서,
 * 여기서 따로 계산하면 같은 로직이 두 벌이 된다.
 */
@Service
@RequiredArgsConstructor
public class GatheringMissionService implements GatheringMissionUseCase {

    /** 한 모임에 걸 수 있는 미션 수 상한. 미션이 너무 많으면 아무도 안 한다. */
    private static final int MAX_MISSIONS_PER_GATHERING = 20;

    private final GatheringMissionRepository missionRepository;
    private final MissionCompletionRepository completionRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final SecurityService securityService;
    private final PointService pointService;
    private final NotificationService notificationService;
    private final ProfanityFilterService profanityFilterService;

    /* ------------------------------------------------------------------ */
    /* 호스트: 미션 출제                                                     */
    /* ------------------------------------------------------------------ */

    @Override
    @Transactional
    public GatheringMissionResponse createMission(Long gatheringId, GatheringMissionRequest request) {
        Gathering gathering = requireHost(gatheringId);

        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미션 내용을 입력해주세요.");
        }
        String title = validateTitle(request.getTitle());
        String description = validateDescription(request.getDescription());
        int rewardPoints = validateRewardPoints(request.getRewardPoints());

        if (missionRepository.countByGatheringId(gatheringId) >= MAX_MISSIONS_PER_GATHERING) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "미션은 모임당 최대 " + MAX_MISSIONS_PER_GATHERING + "개까지 등록할 수 있습니다.");
        }

        boolean requiresPhoto = Boolean.TRUE.equals(request.getRequiresPhoto());
        GatheringMission mission = missionRepository.save(
                GatheringMission.of(gathering, title, description, rewardPoints, requiresPhoto));

        Map<String, Object> created = new HashMap<>();
        created.put("gatheringId", gatheringId);
        created.put("missionId", mission.getId());
        created.put("missionTitle", mission.getTitle());
        notifyCrew(gatheringId, "mission-created", created);

        return GatheringMissionResponse.from(mission);
    }

    @Override
    @Transactional
    public GatheringMissionResponse updateMission(Long gatheringId, Long missionId, GatheringMissionRequest request) {
        requireHost(gatheringId);
        GatheringMission mission = getMissionOf(gatheringId, missionId);

        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미션 내용을 입력해주세요.");
        }
        mission.setTitle(validateTitle(request.getTitle()));
        mission.setDescription(validateDescription(request.getDescription()));
        mission.setRewardPoints(validateRewardPoints(request.getRewardPoints()));
        if (request.getRequiresPhoto() != null) {
            mission.setRequiresPhoto(request.getRequiresPhoto());
        }

        return GatheringMissionResponse.from(missionRepository.save(mission));
    }

    @Override
    @Transactional
    public void deleteMission(Long gatheringId, Long missionId) {
        requireHost(gatheringId);
        GatheringMission mission = getMissionOf(gatheringId, missionId);

        // 이미 승인해서 보상까지 나간 미션은 지우지 않는다.
        // 지워버리면 스탬프북에는 남아 있는데 그 근거가 사라져 설명할 수 없는 스탬프가 된다.
        boolean hasApproved = completionRepository.findByMissionIdOrderByCreatedAtAsc(missionId).stream()
                .anyMatch(c -> c.getStatus() == MissionCompletionStatus.APPROVED);
        if (hasApproved) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "이미 완료한 크루가 있는 미션은 삭제할 수 없습니다.");
        }

        mission.setDeleted(true);
        missionRepository.save(mission);
    }

    /* ------------------------------------------------------------------ */
    /* 호스트: 인증 심사                                                     */
    /* ------------------------------------------------------------------ */

    @Override
    @Transactional(readOnly = true)
    public List<MissionCompletionResponse> getPendingCompletions(Long gatheringId) {
        requireHost(gatheringId);

        List<Long> missionIds = missionRepository.findByGatheringIdOrderByCreatedAtAsc(gatheringId)
                .stream().map(GatheringMission::getId).toList();
        if (missionIds.isEmpty()) {
            return Collections.emptyList();
        }

        return completionRepository
                .findByMissionIdInAndStatusOrderByCreatedAtAsc(missionIds, MissionCompletionStatus.SUBMITTED)
                .stream()
                .map(MissionCompletionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public MissionCompletionResponse approveCompletion(Long gatheringId, Long completionId) {
        requireHost(gatheringId);
        MissionCompletion completion = getCompletionOf(gatheringId, completionId);

        if (completion.getStatus() == MissionCompletionStatus.APPROVED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 승인한 인증입니다.");
        }

        completion.approve();
        completionRepository.save(completion);

        GatheringMission mission = completion.getMission();
        User crew = completion.getUser();

        // 포인트 적립과 스탬프 발급을 한 번에 처리한다.
        // 스탬프 이미지는 크루가 올린 인증 사진을 그대로 쓴다. 내가 찍은 사진이 여권에 박히는 게
        // 시스템이 준 기본 이미지보다 훨씬 남는다.
        pointService.addPoints(
                crew.getId(),
                mission.getRewardPoints(),
                1,
                mission.getTitle(),
                gatheringId,
                completion.getPhotoUrl()
        );

        Map<String, Object> approved = new HashMap<>();
        approved.put("gatheringId", gatheringId);
        approved.put("missionId", mission.getId());
        approved.put("missionTitle", mission.getTitle());
        approved.put("rewardPoints", mission.getRewardPoints());
        notificationService.send(crew.getEmail(), "mission-approved", approved);

        return MissionCompletionResponse.from(completion);
    }

    @Override
    @Transactional
    public MissionCompletionResponse rejectCompletion(Long gatheringId, Long completionId) {
        requireHost(gatheringId);
        MissionCompletion completion = getCompletionOf(gatheringId, completionId);

        if (completion.getStatus() == MissionCompletionStatus.APPROVED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "이미 보상이 지급된 인증은 반려할 수 없습니다.");
        }

        completion.reject();
        completionRepository.save(completion);

        Map<String, Object> rejected = new HashMap<>();
        rejected.put("gatheringId", gatheringId);
        rejected.put("missionId", completion.getMission().getId());
        rejected.put("missionTitle", completion.getMission().getTitle());
        notificationService.send(completion.getUser().getEmail(), "mission-rejected", rejected);

        return MissionCompletionResponse.from(completion);
    }

    /* ------------------------------------------------------------------ */
    /* 크루: 조회와 인증                                                     */
    /* ------------------------------------------------------------------ */

    @Override
    @Transactional(readOnly = true)
    public List<GatheringMissionResponse> getMissions(Long gatheringId) {
        Gathering gathering = getGatheringOrThrow(gatheringId);
        User me = requireCrew(gathering);
        boolean host = isHost(gathering, me);

        List<GatheringMission> missions = missionRepository.findByGatheringIdOrderByCreatedAtAsc(gatheringId);
        if (missions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> missionIds = missions.stream().map(GatheringMission::getId).toList();

        // 미션마다 조회를 날리면 미션 20개에 쿼리 20개가 된다. 한 번에 긁어서 묶는다.
        Map<Long, List<MissionCompletion>> byMission = new HashMap<>();
        for (MissionCompletion completion : completionRepository.findByMissionIdIn(missionIds)) {
            byMission.computeIfAbsent(completion.getMission().getId(), k -> new ArrayList<>()).add(completion);
        }

        List<GatheringMissionResponse> responses = new ArrayList<>();
        for (GatheringMission mission : missions) {
            List<MissionCompletion> completions = byMission.getOrDefault(mission.getId(), List.of());

            GatheringMissionResponse response = GatheringMissionResponse.from(mission);
            response.setApprovedCount((int) completions.stream()
                    .filter(c -> c.getStatus() == MissionCompletionStatus.APPROVED).count());
            response.setPendingCount((int) completions.stream()
                    .filter(c -> c.getStatus() == MissionCompletionStatus.SUBMITTED).count());

            completions.stream()
                    .filter(c -> c.getUser().getId().equals(me.getId()))
                    .findFirst()
                    .ifPresent(mine -> {
                        response.setMyStatus(mine.getStatus());
                        response.setMyCompletionId(mine.getId());
                        response.setMyPhotoUrl(mine.getPhotoUrl());
                        response.setMyMemo(mine.getMemo());
                    });

            if (host) {
                response.setPendingCompletions(completions.stream()
                        .filter(c -> c.getStatus() == MissionCompletionStatus.SUBMITTED)
                        .map(MissionCompletionResponse::from)
                        .toList());
            }

            responses.add(response);
        }
        return responses;
    }

    @Override
    @Transactional
    public MissionCompletionResponse submitCompletion(Long gatheringId, Long missionId, MissionSubmitRequest request) {
        Gathering gathering = getGatheringOrThrow(gatheringId);
        User me = requireCrew(gathering);

        // 호스트는 자기가 낸 미션을 스스로 깰 수 없다.
        // 승인 권한을 가진 사람이 제출까지 하면 심사가 성립하지 않는다.
        if (isHost(gathering, me)) {
            throw new CustomException(ErrorCode.SELF_ACTION_NOT_ALLOWED,
                    "호스트는 자신이 출제한 미션을 인증할 수 없습니다.");
        }

        GatheringMission mission = getMissionOf(gatheringId, missionId);

        String photoUrl = request != null ? trimToNull(request.getPhotoUrl()) : null;
        String memo = request != null ? trimToNull(request.getMemo()) : null;

        if (mission.isRequiresPhoto() && photoUrl == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이 미션은 인증 사진이 필요합니다.");
        }
        if (memo != null) {
            profanityFilterService.validateText(memo);
        }

        MissionCompletion completion = completionRepository
                .findByMissionIdAndUserId(missionId, me.getId())
                .orElse(null);

        if (completion == null) {
            completion = MissionCompletion.of(mission, me, photoUrl, memo);
        } else if (completion.getStatus() == MissionCompletionStatus.APPROVED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 완료한 미션입니다.");
        } else {
            // SUBMITTED 면 심사 전 수정, REJECTED 면 재도전. 어느 쪽이든 기록 하나를 갱신한다.
            completion.resubmit(photoUrl, memo);
        }

        completionRepository.save(completion);

        if (gathering.getHost() != null && gathering.getHost().getEmail() != null) {
            Map<String, Object> submitted = new HashMap<>();
            submitted.put("gatheringId", gatheringId);
            submitted.put("missionId", mission.getId());
            submitted.put("missionTitle", mission.getTitle());
            submitted.put("crewName", me.getName() != null ? me.getName() : me.getEmail());
            notificationService.send(gathering.getHost().getEmail(), "mission-submitted", submitted);
        }

        return MissionCompletionResponse.from(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public MissionProgressResponse getMyProgress(Long gatheringId) {
        Gathering gathering = getGatheringOrThrow(gatheringId);
        User me = requireCrew(gathering);

        List<GatheringMission> missions = missionRepository.findByGatheringIdOrderByCreatedAtAsc(gatheringId);
        int totalCount = missions.size();

        if (totalCount == 0) {
            return MissionProgressResponse.builder()
                    .gatheringId(gatheringId)
                    .totalCount(0)
                    .clearedCount(0)
                    .progressPercentage(0.0)
                    .earnedPoints(0)
                    .build();
        }

        Map<Long, GatheringMission> missionById = new HashMap<>();
        missions.forEach(m -> missionById.put(m.getId(), m));

        List<MissionCompletion> mine = completionRepository
                .findByMissionIdIn(new ArrayList<>(missionById.keySet()))
                .stream()
                .filter(c -> c.getUser().getId().equals(me.getId()))
                .filter(c -> c.getStatus() == MissionCompletionStatus.APPROVED)
                .toList();

        int clearedCount = mine.size();
        int earnedPoints = mine.stream()
                .mapToInt(c -> missionById.get(c.getMission().getId()).getRewardPoints())
                .sum();
        double percentage = (double) clearedCount / totalCount * 100.0;

        return MissionProgressResponse.builder()
                .gatheringId(gatheringId)
                .totalCount(totalCount)
                .clearedCount(clearedCount)
                .progressPercentage(Math.round(percentage * 10.0) / 10.0)
                .earnedPoints(earnedPoints)
                .build();
    }

    /* ------------------------------------------------------------------ */
    /* 내부 검증                                                            */
    /* ------------------------------------------------------------------ */

    private Gathering getGatheringOrThrow(Long gatheringId) {
        if (gatheringId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "모임 ID가 올바르지 않습니다.");
        }
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
    }

    private boolean isHost(Gathering gathering, User user) {
        return gathering.getHost() != null && gathering.getHost().getId().equals(user.getId());
    }

    /** 호스트 전용 동작을 지키는 관문. 통과하면 모임을 돌려준다. */
    private Gathering requireHost(Long gatheringId) {
        Gathering gathering = getGatheringOrThrow(gatheringId);
        User me = securityService.getCurrentUser();
        if (!isHost(gathering, me)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "모임 호스트만 미션을 관리할 수 있습니다.");
        }
        return gathering;
    }

    /** 호스트이거나 승인된 크루여야 통과한다. 통과하면 요청자를 돌려준다. */
    private User requireCrew(Gathering gathering) {
        User me = securityService.getCurrentUser();
        if (isHost(gathering, me)) {
            return me;
        }
        GatheringMember member = gatheringMemberRepository
                .findByGatheringIdAndUserId(gathering.getId(), me.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN_ACTION,
                        "모임 크루만 미션에 참여할 수 있습니다."));
        if (member.getStatus() != MemberStatus.APPROVED) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION,
                    "탑승이 승인된 크루만 미션에 참여할 수 있습니다.");
        }
        return me;
    }

    /** 다른 모임의 미션 id 를 끼워 넣어 접근하는 것을 막는다. */
    private GatheringMission getMissionOf(Long gatheringId, Long missionId) {
        if (missionId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미션 ID가 올바르지 않습니다.");
        }
        GatheringMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
        if (mission.getGathering() == null || !mission.getGathering().getId().equals(gatheringId)) {
            throw new CustomException(ErrorCode.MISSION_NOT_FOUND, "해당 모임의 미션이 아닙니다.");
        }
        return mission;
    }

    private MissionCompletion getCompletionOf(Long gatheringId, Long completionId) {
        if (completionId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미션 인증 ID가 올바르지 않습니다.");
        }
        MissionCompletion completion = completionRepository.findById(completionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_COMPLETION_NOT_FOUND));
        GatheringMission mission = completion.getMission();
        if (mission == null || mission.getGathering() == null
                || !mission.getGathering().getId().equals(gatheringId)) {
            throw new CustomException(ErrorCode.MISSION_COMPLETION_NOT_FOUND, "해당 모임의 인증 내역이 아닙니다.");
        }
        return completion;
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미션 제목을 입력해주세요.");
        }
        String trimmed = title.trim();
        if (trimmed.length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미션 제목은 100자를 넘을 수 없습니다.");
        }
        profanityFilterService.validateText(trimmed);
        return trimmed;
    }

    private String validateDescription(String description) {
        String trimmed = trimToNull(description);
        if (trimmed == null) {
            return null;
        }
        if (trimmed.length() > 500) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미션 설명은 500자를 넘을 수 없습니다.");
        }
        profanityFilterService.validateText(trimmed);
        return trimmed;
    }

    private int validateRewardPoints(Integer rewardPoints) {
        if (rewardPoints == null) {
            return GatheringMission.DEFAULT_REWARD_POINTS;
        }
        if (rewardPoints < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "보상 포인트는 0보다 작을 수 없습니다.");
        }
        if (rewardPoints > GatheringMission.MAX_REWARD_POINTS) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "보상 포인트는 " + GatheringMission.MAX_REWARD_POINTS + "점을 넘을 수 없습니다.");
        }
        return rewardPoints;
    }

    /**
     * 알림 페이로드는 Map.of 로 만들지 않는다.
     * Map.of 는 값에 null 이 하나라도 있으면 NullPointerException 을 던져서,
     * 알림이 본래 하려던 일(부수적인 통지)이 정작 본 작업을 되돌려 버린다.
     */
    private void notifyCrew(Long gatheringId, String eventName, Object payload) {
        notificationService.sendToAllMembers(gatheringId, eventName, payload);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
