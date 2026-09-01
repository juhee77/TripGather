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
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringMissionRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.MissionCompletionRepository;
import com.example.demo.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GatheringMissionServiceTest {

    @Mock private GatheringMissionRepository missionRepository;
    @Mock private MissionCompletionRepository completionRepository;
    @Mock private GatheringRepository gatheringRepository;
    @Mock private GatheringMemberRepository gatheringMemberRepository;
    @Mock private SecurityService securityService;
    @Mock private PointService pointService;
    @Mock private NotificationService notificationService;
    @Mock private ProfanityFilterService profanityFilterService;

    @InjectMocks private GatheringMissionService missionService;

    private static final Long GATHERING_ID = 1L;
    private static final Long MISSION_ID = 10L;

    private User host;
    private User crew;
    private Gathering gathering;

    @BeforeEach
    void setUp() {
        host = User.builder().id(100L).name("호스트").email("host@test.com").build();
        crew = User.builder().id(200L).name("크루").email("crew@test.com").build();
        gathering = Gathering.builder().id(GATHERING_ID).title("제주 한 바퀴").host(host).build();
    }

    private GatheringMission mission(boolean requiresPhoto, int rewardPoints) {
        return GatheringMission.builder()
                .id(MISSION_ID)
                .gathering(gathering)
                .title("흑돼지 먹기")
                .rewardPoints(rewardPoints)
                .requiresPhoto(requiresPhoto)
                .build();
    }

    private void loginAsHost() {
        given(securityService.getCurrentUser()).willReturn(host);
    }

    private void loginAsApprovedCrew() {
        given(securityService.getCurrentUser()).willReturn(crew);
        given(gatheringMemberRepository.findByGatheringIdAndUserId(GATHERING_ID, crew.getId()))
                .willReturn(Optional.of(GatheringMember.builder()
                        .gathering(gathering).user(crew).status(MemberStatus.APPROVED).build()));
    }

    @Nested
    @DisplayName("미션 출제")
    class CreateMission {

        @Test
        @DisplayName("호스트가 미션을 출제하면 기본 보상 50포인트가 적용된다")
        void create_Success_WithDefaultReward() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            given(missionRepository.countByGatheringId(GATHERING_ID)).willReturn(0L);
            given(missionRepository.save(any(GatheringMission.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            GatheringMissionRequest request = GatheringMissionRequest.builder()
                    .title("흑돼지 먹기")
                    .description("제주 흑돼지를 먹고 인증한다")
                    .build();

            // when
            GatheringMissionResponse response = missionService.createMission(GATHERING_ID, request);

            // then
            assertThat(response.getTitle()).isEqualTo("흑돼지 먹기");
            assertThat(response.getRewardPoints()).isEqualTo(GatheringMission.DEFAULT_REWARD_POINTS);
            assertThat(response.isRequiresPhoto()).isFalse();
        }

        @Test
        @DisplayName("호스트가 아니면 미션을 출제할 수 없다")
        void create_Fail_WhenNotHost() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            given(securityService.getCurrentUser()).willReturn(crew);

            GatheringMissionRequest request = GatheringMissionRequest.builder().title("흑돼지 먹기").build();

            // when & then
            assertThatThrownBy(() -> missionService.createMission(GATHERING_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("호스트만");

            verify(missionRepository, never()).save(any());
        }

        @Test
        @DisplayName("제목이 비어 있으면 미션을 출제할 수 없다")
        void create_Fail_WhenTitleBlank() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            GatheringMissionRequest request = GatheringMissionRequest.builder().title("   ").build();

            // when & then
            assertThatThrownBy(() -> missionService.createMission(GATHERING_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("미션 제목");
        }

        @Test
        @DisplayName("보상 포인트가 상한을 넘으면 미션을 출제할 수 없다")
        void create_Fail_WhenRewardTooLarge() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            GatheringMissionRequest request = GatheringMissionRequest.builder()
                    .title("흑돼지 먹기")
                    .rewardPoints(GatheringMission.MAX_REWARD_POINTS + 1)
                    .build();

            // when & then
            assertThatThrownBy(() -> missionService.createMission(GATHERING_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("보상 포인트");
        }
    }

    @Nested
    @DisplayName("미션 인증 제출")
    class SubmitCompletion {

        @Test
        @DisplayName("승인된 크루가 인증을 올리면 심사 대기 상태가 된다")
        void submit_Success() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(mission(false, 50)));
            given(completionRepository.findByMissionIdAndUserId(MISSION_ID, crew.getId()))
                    .willReturn(Optional.empty());
            given(completionRepository.save(any(MissionCompletion.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            MissionSubmitRequest request = MissionSubmitRequest.builder().memo("맛있었다").build();

            // when
            MissionCompletionResponse response = missionService.submitCompletion(GATHERING_ID, MISSION_ID, request);

            // then
            assertThat(response.getStatus()).isEqualTo(MissionCompletionStatus.SUBMITTED);
            assertThat(response.getMemo()).isEqualTo("맛있었다");
            verify(notificationService).send(eq(host.getEmail()), eq("mission-submitted"), any());
        }

        @Test
        @DisplayName("사진이 필수인 미션에 사진 없이 인증하면 거부된다")
        void submit_Fail_WhenPhotoRequiredButMissing() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(mission(true, 50)));

            MissionSubmitRequest request = MissionSubmitRequest.builder().memo("사진은 없다").build();

            // when & then
            assertThatThrownBy(() -> missionService.submitCompletion(GATHERING_ID, MISSION_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("인증 사진");

            verify(completionRepository, never()).save(any());
        }

        @Test
        @DisplayName("호스트는 자신이 출제한 미션을 인증할 수 없다")
        void submit_Fail_WhenHostSubmits() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            // when & then
            assertThatThrownBy(() ->
                    missionService.submitCompletion(GATHERING_ID, MISSION_ID, MissionSubmitRequest.builder().build()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("호스트는");
        }

        @Test
        @DisplayName("승인 대기 중인 크루는 미션에 참여할 수 없다")
        void submit_Fail_WhenMemberNotApproved() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            given(securityService.getCurrentUser()).willReturn(crew);
            given(gatheringMemberRepository.findByGatheringIdAndUserId(GATHERING_ID, crew.getId()))
                    .willReturn(Optional.of(GatheringMember.builder()
                            .gathering(gathering).user(crew).status(MemberStatus.PENDING).build()));

            // when & then
            assertThatThrownBy(() ->
                    missionService.submitCompletion(GATHERING_ID, MISSION_ID, MissionSubmitRequest.builder().build()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("승인된 크루");
        }

        @Test
        @DisplayName("이미 완료한 미션은 다시 인증할 수 없다")
        void submit_Fail_WhenAlreadyApproved() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();
            GatheringMission target = mission(false, 50);
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(target));

            MissionCompletion approved = MissionCompletion.of(target, crew, null, "이전 인증");
            approved.approve();
            given(completionRepository.findByMissionIdAndUserId(MISSION_ID, crew.getId()))
                    .willReturn(Optional.of(approved));

            // when & then
            assertThatThrownBy(() ->
                    missionService.submitCompletion(GATHERING_ID, MISSION_ID, MissionSubmitRequest.builder().build()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("이미 완료한 미션");
        }

        @Test
        @DisplayName("반려당한 인증은 기록을 새로 만들지 않고 갱신해서 재제출한다")
        void submit_Success_WhenResubmitAfterReject() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();
            GatheringMission target = mission(false, 50);
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(target));

            MissionCompletion rejected = MissionCompletion.of(target, crew, null, "예전 메모");
            rejected.setId(999L);
            rejected.reject();
            given(completionRepository.findByMissionIdAndUserId(MISSION_ID, crew.getId()))
                    .willReturn(Optional.of(rejected));
            given(completionRepository.save(any(MissionCompletion.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            MissionSubmitRequest request = MissionSubmitRequest.builder().memo("다시 찍었다").build();

            // when
            MissionCompletionResponse response = missionService.submitCompletion(GATHERING_ID, MISSION_ID, request);

            // then
            assertThat(response.getId()).isEqualTo(999L);
            assertThat(response.getStatus()).isEqualTo(MissionCompletionStatus.SUBMITTED);
            assertThat(response.getMemo()).isEqualTo("다시 찍었다");
            assertThat(response.getReviewedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("인증 심사")
    class ReviewCompletion {

        private MissionCompletion submittedCompletion(int rewardPoints, String photoUrl) {
            MissionCompletion completion =
                    MissionCompletion.of(mission(false, rewardPoints), crew, photoUrl, "인증합니다");
            completion.setId(500L);
            return completion;
        }

        @Test
        @DisplayName("호스트가 승인하면 보상 포인트와 스탬프가 지급된다")
        void approve_Success_AwardsPointsAndStamp() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            MissionCompletion completion = submittedCompletion(80, "photo.png");
            given(completionRepository.findById(500L)).willReturn(Optional.of(completion));
            given(completionRepository.save(any(MissionCompletion.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            MissionCompletionResponse response = missionService.approveCompletion(GATHERING_ID, 500L);

            // then
            assertThat(response.getStatus()).isEqualTo(MissionCompletionStatus.APPROVED);
            assertThat(response.getReviewedAt()).isNotNull();

            // 인증 사진이 그대로 스탬프 이미지가 된다
            verify(pointService).addPoints(crew.getId(), 80, 1, "흑돼지 먹기", GATHERING_ID, "photo.png");
            verify(notificationService).send(eq(crew.getEmail()), eq("mission-approved"), any());
        }

        @Test
        @DisplayName("같은 인증을 두 번 승인해서 보상을 중복 지급할 수 없다")
        void approve_Fail_WhenAlreadyApproved() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            MissionCompletion completion = submittedCompletion(80, "photo.png");
            completion.approve();
            given(completionRepository.findById(500L)).willReturn(Optional.of(completion));

            // when & then
            assertThatThrownBy(() -> missionService.approveCompletion(GATHERING_ID, 500L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("이미 승인");

            verify(pointService, never())
                    .addPoints(anyLong(), anyInt(), anyInt(), anyString(), anyLong(), anyString());
        }

        @Test
        @DisplayName("호스트가 아니면 인증을 심사할 수 없다")
        void approve_Fail_WhenNotHost() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            given(securityService.getCurrentUser()).willReturn(crew);

            // when & then
            assertThatThrownBy(() -> missionService.approveCompletion(GATHERING_ID, 500L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("호스트만");
        }

        @Test
        @DisplayName("이미 보상이 나간 인증은 반려할 수 없다")
        void reject_Fail_WhenAlreadyApproved() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            MissionCompletion completion = submittedCompletion(80, "photo.png");
            completion.approve();
            given(completionRepository.findById(500L)).willReturn(Optional.of(completion));

            // when & then
            assertThatThrownBy(() -> missionService.rejectCompletion(GATHERING_ID, 500L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("이미 보상이 지급된");
        }
    }

    @Nested
    @DisplayName("미션 수정과 삭제")
    class ModifyMission {

        @Test
        @DisplayName("호스트가 미션 내용과 보상을 바꿀 수 있다")
        void update_Success() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(mission(false, 50)));
            given(missionRepository.save(any(GatheringMission.class))).willAnswer(inv -> inv.getArgument(0));

            GatheringMissionRequest request = GatheringMissionRequest.builder()
                    .title("흑돼지 대신 갈치조림")
                    .description("바뀐 설명")
                    .rewardPoints(120)
                    .requiresPhoto(true)
                    .build();

            GatheringMissionResponse response = missionService.updateMission(GATHERING_ID, MISSION_ID, request);

            assertThat(response.getTitle()).isEqualTo("흑돼지 대신 갈치조림");
            assertThat(response.getDescription()).isEqualTo("바뀐 설명");
            assertThat(response.getRewardPoints()).isEqualTo(120);
            assertThat(response.isRequiresPhoto()).isTrue();
        }

        @Test
        @DisplayName("설명이 500자를 넘으면 거부된다")
        void update_Fail_WhenDescriptionTooLong() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(mission(false, 50)));

            GatheringMissionRequest request = GatheringMissionRequest.builder()
                    .title("긴 설명 미션")
                    .description("가".repeat(501))
                    .build();

            assertThatThrownBy(() -> missionService.updateMission(GATHERING_ID, MISSION_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("500자");
        }

        @Test
        @DisplayName("아무도 완료하지 않은 미션은 논리 삭제된다")
        void delete_Success() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            GatheringMission target = mission(false, 50);
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(target));
            given(completionRepository.findByMissionIdOrderByCreatedAtAsc(MISSION_ID)).willReturn(List.of());

            missionService.deleteMission(GATHERING_ID, MISSION_ID);

            assertThat(target.isDeleted()).isTrue();
            verify(missionRepository).save(target);
        }

        @Test
        @DisplayName("이미 보상이 나간 미션은 삭제되지 않는다")
        void delete_Fail_WhenSomeoneCleared() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            GatheringMission target = mission(false, 50);
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(target));

            MissionCompletion approved = MissionCompletion.of(target, crew, null, null);
            approved.approve();
            given(completionRepository.findByMissionIdOrderByCreatedAtAsc(MISSION_ID)).willReturn(List.of(approved));

            assertThatThrownBy(() -> missionService.deleteMission(GATHERING_ID, MISSION_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("삭제할 수 없습니다");

            assertThat(target.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("다른 모임의 미션 ID로는 접근할 수 없다")
        void update_Fail_WhenMissionBelongsToAnotherGathering() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            Gathering other = Gathering.builder().id(99L).host(host).build();
            GatheringMission foreign = GatheringMission.builder()
                    .id(MISSION_ID).gathering(other).title("남의 미션").build();
            given(missionRepository.findById(MISSION_ID)).willReturn(Optional.of(foreign));

            assertThatThrownBy(() -> missionService.updateMission(
                    GATHERING_ID, MISSION_ID, GatheringMissionRequest.builder().title("바꾸기").build()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("해당 모임의 미션이 아닙니다");
        }
    }

    @Nested
    @DisplayName("미션 목록 조회")
    class ListMissions {

        @Test
        @DisplayName("크루에게는 내 인증 상태가 함께 담긴다")
        void getMissions_Success_ForCrew() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();

            GatheringMission target = mission(false, 50);
            given(missionRepository.findByGatheringIdOrderByCreatedAtAsc(GATHERING_ID))
                    .willReturn(List.of(target));

            MissionCompletion mine = MissionCompletion.of(target, crew, "my.png", "내 메모");
            mine.setId(7L);
            User other = User.builder().id(300L).name("다른크루").email("other@test.com").build();
            MissionCompletion theirs = MissionCompletion.of(target, other, null, null);
            theirs.approve();
            given(completionRepository.findByMissionIdIn(anyList())).willReturn(List.of(mine, theirs));

            List<GatheringMissionResponse> responses = missionService.getMissions(GATHERING_ID);

            assertThat(responses).hasSize(1);
            GatheringMissionResponse response = responses.get(0);
            assertThat(response.getApprovedCount()).isEqualTo(1);
            assertThat(response.getPendingCount()).isEqualTo(1);
            assertThat(response.getMyStatus()).isEqualTo(MissionCompletionStatus.SUBMITTED);
            assertThat(response.getMyCompletionId()).isEqualTo(7L);
            assertThat(response.getMyPhotoUrl()).isEqualTo("my.png");
            assertThat(response.getMyMemo()).isEqualTo("내 메모");
            // 크루에게 남의 심사 대기 목록을 넘기지 않는다
            assertThat(response.getPendingCompletions()).isNull();
        }

        @Test
        @DisplayName("호스트에게만 심사 대기 목록이 함께 담긴다")
        void getMissions_Success_ForHost() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            GatheringMission target = mission(false, 50);
            given(missionRepository.findByGatheringIdOrderByCreatedAtAsc(GATHERING_ID))
                    .willReturn(List.of(target));
            given(completionRepository.findByMissionIdIn(anyList()))
                    .willReturn(List.of(MissionCompletion.of(target, crew, "p.png", "봐주세요")));

            List<GatheringMissionResponse> responses = missionService.getMissions(GATHERING_ID);

            assertThat(responses.get(0).getPendingCompletions()).hasSize(1);
            assertThat(responses.get(0).getPendingCompletions().get(0).getUserName()).isEqualTo("크루");
        }

        @Test
        @DisplayName("미션이 없으면 빈 목록을 돌려준다")
        void getMissions_Success_WhenEmpty() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();
            given(missionRepository.findByGatheringIdOrderByCreatedAtAsc(GATHERING_ID)).willReturn(List.of());

            assertThat(missionService.getMissions(GATHERING_ID)).isEmpty();
        }

        @Test
        @DisplayName("모임 크루가 아니면 목록을 볼 수 없다")
        void getMissions_Fail_WhenNotCrew() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            given(securityService.getCurrentUser()).willReturn(crew);
            given(gatheringMemberRepository.findByGatheringIdAndUserId(GATHERING_ID, crew.getId()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> missionService.getMissions(GATHERING_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("모임 크루만");
        }

        @Test
        @DisplayName("존재하지 않는 모임이면 조회할 수 없다")
        void getMissions_Fail_WhenGatheringNotFound() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> missionService.getMissions(GATHERING_ID))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("호스트 심사 대기 목록")
    class PendingList {

        @Test
        @DisplayName("심사 대기 중인 인증만 모아서 돌려준다")
        void getPending_Success() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            GatheringMission target = mission(false, 50);
            given(missionRepository.findByGatheringIdOrderByCreatedAtAsc(GATHERING_ID))
                    .willReturn(List.of(target));
            given(completionRepository.findByMissionIdInAndStatusOrderByCreatedAtAsc(
                    anyList(), eq(MissionCompletionStatus.SUBMITTED)))
                    .willReturn(List.of(MissionCompletion.of(target, crew, "p.png", "확인 부탁")));

            List<MissionCompletionResponse> responses = missionService.getPendingCompletions(GATHERING_ID);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getMemo()).isEqualTo("확인 부탁");
        }

        @Test
        @DisplayName("미션이 하나도 없으면 빈 목록을 돌려준다")
        void getPending_Success_WhenNoMissions() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            given(missionRepository.findByGatheringIdOrderByCreatedAtAsc(GATHERING_ID)).willReturn(List.of());

            assertThat(missionService.getPendingCompletions(GATHERING_ID)).isEmpty();
        }

        @Test
        @DisplayName("호스트가 반려하면 크루에게 알림이 간다")
        void reject_Success() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();
            MissionCompletion completion = MissionCompletion.of(mission(false, 50), crew, "p.png", "인증");
            completion.setId(501L);
            given(completionRepository.findById(501L)).willReturn(Optional.of(completion));
            given(completionRepository.save(any(MissionCompletion.class))).willAnswer(inv -> inv.getArgument(0));

            MissionCompletionResponse response = missionService.rejectCompletion(GATHERING_ID, 501L);

            assertThat(response.getStatus()).isEqualTo(MissionCompletionStatus.REJECTED);
            assertThat(response.getReviewedAt()).isNotNull();
            verify(notificationService).send(eq(crew.getEmail()), eq("mission-rejected"), any());
            verify(pointService, never())
                    .addPoints(anyLong(), anyInt(), anyInt(), anyString(), anyLong(), anyString());
        }

        @Test
        @DisplayName("다른 모임의 인증 ID로는 심사할 수 없다")
        void approve_Fail_WhenCompletionBelongsToAnotherGathering() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            Gathering other = Gathering.builder().id(99L).host(host).build();
            GatheringMission foreign = GatheringMission.builder()
                    .id(88L).gathering(other).title("남의 미션").build();
            MissionCompletion completion = MissionCompletion.of(foreign, crew, null, null);
            completion.setId(502L);
            given(completionRepository.findById(502L)).willReturn(Optional.of(completion));

            assertThatThrownBy(() -> missionService.approveCompletion(GATHERING_ID, 502L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("해당 모임의 인증 내역이 아닙니다");
        }

        @Test
        @DisplayName("인증 ID가 없으면 심사할 수 없다")
        void approve_Fail_WhenCompletionIdNull() {
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsHost();

            assertThatThrownBy(() -> missionService.approveCompletion(GATHERING_ID, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("미션 인증 ID");
        }
    }

    @Nested
    @DisplayName("진행도 조회")
    class Progress {

        @Test
        @DisplayName("미션 2개 중 1개를 승인받으면 진행률 50%와 획득 포인트가 계산된다")
        void getMyProgress_Success() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();

            GatheringMission cleared = mission(false, 80);
            GatheringMission remaining = GatheringMission.builder()
                    .id(11L).gathering(gathering).title("성산일출봉 일출").rewardPoints(50).build();
            given(missionRepository.findByGatheringIdOrderByCreatedAtAsc(GATHERING_ID))
                    .willReturn(List.of(cleared, remaining));

            MissionCompletion approved = MissionCompletion.of(cleared, crew, "p.png", null);
            approved.approve();
            given(completionRepository.findByMissionIdIn(anyList())).willReturn(List.of(approved));

            // when
            MissionProgressResponse response = missionService.getMyProgress(GATHERING_ID);

            // then
            assertThat(response.getTotalCount()).isEqualTo(2);
            assertThat(response.getClearedCount()).isEqualTo(1);
            assertThat(response.getProgressPercentage()).isEqualTo(50.0);
            assertThat(response.getEarnedPoints()).isEqualTo(80);
        }

        @Test
        @DisplayName("미션이 없으면 0으로 나누지 않고 0%를 돌려준다")
        void getMyProgress_Success_WhenNoMissions() {
            // given
            given(gatheringRepository.findById(GATHERING_ID)).willReturn(Optional.of(gathering));
            loginAsApprovedCrew();
            given(missionRepository.findByGatheringIdOrderByCreatedAtAsc(GATHERING_ID)).willReturn(List.of());

            // when
            MissionProgressResponse response = missionService.getMyProgress(GATHERING_ID);

            // then
            assertThat(response.getTotalCount()).isZero();
            assertThat(response.getProgressPercentage()).isZero();
            assertThat(response.getEarnedPoints()).isZero();
        }
    }
}
