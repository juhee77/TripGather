package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.dto.UserMissionResponse;
import com.example.demo.dto.UserMissionStepResponse;
import com.example.demo.dto.StampResponse;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.UserMissionRepository;
import com.example.demo.repository.UserMissionStepRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserMissionServiceImplTest {

    @Mock
    private UserMissionRepository missionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItineraryRepository itineraryRepository;
    @Mock
    private UserMissionStepRepository stepRepository;
    @Mock
    private FileService fileService;
    @Mock
    private PointService pointService;

    @InjectMocks
    private UserMissionServiceImpl userMissionService;

    @Test
    @DisplayName("미션 시작 성공 - 새로운 미션과 하위 단계 생성 확인")
    void startMission_Success() {
        // given
        Long itineraryId = 1L;
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().id(itineraryId).title("Test Trip").build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(itineraryRepository.findById(itineraryId)).willReturn(Optional.of(itinerary));
        given(missionRepository.findByUserIdAndItineraryId(user.getId(), itineraryId)).willReturn(Optional.empty());
        given(missionRepository.save(any(UserMission.class))).willAnswer(i -> i.getArgument(0));

        // when
        UserMissionResponse response = userMissionService.startMission(itineraryId, email);

        // then
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        verify(missionRepository).save(any(UserMission.class));
        verify(stepRepository).saveAll(any());
    }

    @Test
    @DisplayName("미션 단계 완료 성공 - 사진 URL 및 완료 상태 업데이트 확인")
    void completeStep_Success() {
        // given
        Long missionId = 10L;
        Long stepId = 100L;
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().id(1L).build();
        UserMission mission = UserMission.builder().id(missionId).user(user).itinerary(itinerary).build();
        UserMissionStep step = UserMissionStep.builder().id(stepId).userMission(mission).isCompleted(false).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(missionRepository.findById(missionId)).willReturn(Optional.of(mission));
        given(stepRepository.findById(stepId)).willReturn(Optional.of(step));
        given(stepRepository.save(any(UserMissionStep.class))).willAnswer(i -> i.getArgument(0));

        // when
        UserMissionStepResponse response = userMissionService.completeStep(missionId, stepId, "Memo", "http://photo.url", email);

        // then
        assertThat(response.isCompleted()).isTrue();
        assertThat(step.getPhotoUrl()).isEqualTo("http://photo.url");
        verify(stepRepository).save(step);
    }

    @Test
    @DisplayName("미션 전체 완료 성공 - 상태가 COMPLETED로 변경되는지 확인")
    void completeMission_Success() {
        // given
        Long missionId = 10L;
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().title("Final Trip").build();
        UserMission mission = UserMission.builder().id(missionId).user(user).itinerary(itinerary).status(MissionStatus.ACTIVE).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(missionRepository.findById(missionId)).willReturn(Optional.of(mission));
        given(missionRepository.save(any(UserMission.class))).willAnswer(i -> i.getArgument(0));

        // when
        UserMissionResponse response = userMissionService.completeMission(missionId, email);

        // then
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(mission.getCompletedAt()).isNotNull();
        assertThat(mission.getCompletedAt()).isNotNull();
        verify(missionRepository).save(mission);
    }

    @Test
    @DisplayName("내 모든 미션 조회 성공")
    void getMyMissions_Success() {
        // given
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().id(1L).title("Test Trip").build();
        UserMission mission = UserMission.builder().id(10L).user(user).itinerary(itinerary).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(missionRepository.findByUserId(user.getId())).willReturn(java.util.List.of(mission));

        // when
        java.util.List<UserMissionResponse> responses = userMissionService.getMyMissions(email);

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("내 스탬프 조회 성공")
    void getMyStamps_Success() {
        // given
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().id(1L).title("Test Trip").build();
        UserMission mission = UserMission.builder().id(10L).user(user).itinerary(itinerary).status(MissionStatus.COMPLETED).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(missionRepository.findByUserId(user.getId())).willReturn(java.util.List.of(mission));

        // when
        java.util.List<StampResponse> responses = userMissionService.getMyStamps(email);

        // then
        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("미션 중단 요청 성공")
    void requestLeave_Success() {
        // given
        Long missionId = 10L;
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().id(1L).build();
        UserMission mission = UserMission.builder().id(missionId).user(user).status(MissionStatus.ACTIVE).itinerary(itinerary).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(missionRepository.findById(missionId)).willReturn(Optional.of(mission));

        // when
        userMissionService.requestLeave(missionId, email);

        // then
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.LEAVE_REQUESTED);
        verify(missionRepository).save(mission);
    }

    @Test
    @DisplayName("미션 중단 승인 성공")
    void approveLeave_Success() {
        // given
        Long missionId = 10L;
        String hostEmail = "host@test.com";
        User host = User.builder().id(2L).email(hostEmail).name("Host").build();
        Itinerary itinerary = Itinerary.builder().author("Host").routePoints(java.util.List.of()).build();
        UserMission mission = UserMission.builder().id(missionId).itinerary(itinerary).build();

        given(userRepository.findByEmail(hostEmail)).willReturn(Optional.of(host));
        given(missionRepository.findById(missionId)).willReturn(Optional.of(mission));

        // when
        userMissionService.approveLeave(missionId, hostEmail);

        // then
        verify(missionRepository).delete(mission);
    }

    @Test
    @DisplayName("개별 미션 상세 조회 성공")
    void getMission_Success() {
        // given
        Long missionId = 10L;
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().id(1L).build();
        UserMission mission = UserMission.builder().id(missionId).user(user).itinerary(itinerary).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(missionRepository.findById(missionId)).willReturn(Optional.of(mission));

        // when
        UserMissionResponse response = userMissionService.getMission(missionId, email);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("미션 시작 시 이미 존재하면 기존 미션 반환")
    void startMission_ReturnExisting() {
        // given
        Long itineraryId = 1L;
        String email = "test@test.com";
        User user = User.builder().id(1L).email(email).build();
        Itinerary itinerary = Itinerary.builder().id(itineraryId).build();
        UserMission mission = UserMission.builder().id(10L).user(user).itinerary(itinerary).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(itineraryRepository.findById(itineraryId)).willReturn(Optional.of(itinerary));
        given(missionRepository.findByUserIdAndItineraryId(user.getId(), itineraryId)).willReturn(Optional.of(mission));
        given(stepRepository.findByUserMissionId(mission.getId())).willReturn(java.util.List.of());

        // when
        UserMissionResponse response = userMissionService.startMission(itineraryId, email);

        // then
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("미션 단계 완료 실패 - 권한 없음")
    void completeStep_Forbidden() {
        // given
        Long missionId = 10L;
        Long stepId = 100L;
        String email = "other@test.com";
        User owner = User.builder().id(1L).build();
        User other = User.builder().id(2L).email(email).build();
        UserMission mission = UserMission.builder().id(missionId).user(owner).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(other));
        given(missionRepository.findById(missionId)).willReturn(Optional.of(mission));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(com.example.demo.exception.CustomException.class, () ->
            userMissionService.completeStep(missionId, stepId, "Memo", "url", email)
        );
    }

    @Test
    @DisplayName("미션 중단 승인 실패 - 권한 없음")
    void approveLeave_Forbidden() {
        // given
        Long missionId = 10L;
        String otherEmail = "other@test.com";
        User other = User.builder().id(3L).email(otherEmail).name("Other").build();
        Itinerary itinerary = Itinerary.builder().author("Host").routePoints(java.util.List.of()).build();
        UserMission mission = UserMission.builder().id(missionId).itinerary(itinerary).build();

        given(userRepository.findByEmail(otherEmail)).willReturn(Optional.of(other));
        given(missionRepository.findById(missionId)).willReturn(Optional.of(mission));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(com.example.demo.exception.CustomException.class, () ->
            userMissionService.approveLeave(missionId, otherEmail)
        );
    }
}
