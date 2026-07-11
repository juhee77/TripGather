package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.GatheringStatus;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.domain.Itinerary;
import java.util.List;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatheringMemberServiceTest {

    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private GatheringMemberRepository gatheringMemberRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private GatheringMemberService gatheringMemberService;

    @Test
    @DisplayName("모임 참가 신청 성공")
    void joinGathering_Success() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(5).members(new ArrayList<>()).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUser()).willReturn(guest);
        given(gatheringRepository.save(any())).willReturn(gathering);

        // when
        Gathering result = gatheringMemberService.joinGathering(10L);

        // then
        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).getStatus()).isEqualTo(MemberStatus.PENDING);
        verify(gatheringMemberRepository).save(any());
    }

    @Test
    @DisplayName("호스트가 본인 모임에 참가 신청 시 예외 발생")
    void joinGathering_HostCannotJoinOwn_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).members(new ArrayList<>()).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUser()).willReturn(host);

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.joinGathering(10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }
    
    @Test
    @DisplayName("멤버 승인 성공")
    void approveMember_Success() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(5).members(new ArrayList<>()).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(guest).status(MemberStatus.PENDING).build();
        gathering.getMembers().add(member);

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 2L)).willReturn(Optional.of(member));

        // when
        gatheringMemberService.approveMember(10L, 2L);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(gathering.getCurrentJoining()).isEqualTo(1);
    }

    @Test
    @DisplayName("가입 신청 목록 조회 성공")
    void getJoinedGatherings_Success() {
        // given
        User user = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).title("Test Gathering").build();
        given(securityService.getCurrentUserEmail()).willReturn("guest@test.com");
        given(gatheringRepository.findJoinedGatherings("guest@test.com")).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringMemberService.getJoinedGatherings();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Gathering");
    }

    @Test
    @DisplayName("이미 참가 신청한 멤버가 다시 참가 신청 시 새로운 멤버 생성 없이 기존 정보 그대로 반환")
    void joinGathering_AlreadyApplied_DoesNothing() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(5).members(new ArrayList<>()).build();
        GatheringMember existingMember = GatheringMember.builder().gathering(gathering).user(guest).status(MemberStatus.PENDING).build();
        gathering.getMembers().add(existingMember);

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUser()).willReturn(guest);
        given(gatheringRepository.save(any())).willReturn(gathering);

        // when
        Gathering result = gatheringMemberService.joinGathering(10L);

        // then
        assertThat(result.getMembers()).hasSize(1);
        verify(gatheringMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("정원 초과인 모임에 참가 신청 시 예외 발생")
    void joinGathering_Full_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(0).members(new ArrayList<>()).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUser()).willReturn(guest);

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.joinGathering(10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("호스트가 아닌 유저가 멤버 승인 시도 시 예외 발생")
    void approveMember_NotHost_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("not-host@test.com");

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.approveMember(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACTION);
    }

    @Test
    @DisplayName("존재하지 않는 멤버 승인 시도 시 예외 발생")
    void approveMember_MemberNotFound_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.approveMember(10L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("호스트 본인을 승인 시도 시 예외 발생")
    void approveMember_HostThemselves_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(host).status(MemberStatus.PENDING).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 1L)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.approveMember(10L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SELF_ACTION_NOT_ALLOWED);
    }

    @Test
    @DisplayName("멤버 승인 후 정원이 가득 찬 경우 모임 상태가 CLOSED로 전환")
    void approveMember_ClosedWhenFull() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(1).status(GatheringStatus.OPEN).members(new ArrayList<>()).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(guest).status(MemberStatus.PENDING).build();
        gathering.getMembers().add(member);

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 2L)).willReturn(Optional.of(member));

        // when
        gatheringMemberService.approveMember(10L, 2L);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(gathering.getStatus()).isEqualTo(GatheringStatus.CLOSED);
    }

    @Test
    @DisplayName("멤버 거절 성공")
    void rejectMember_Success() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).members(new ArrayList<>()).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(guest).status(MemberStatus.PENDING).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 2L)).willReturn(Optional.of(member));

        // when
        gatheringMemberService.rejectMember(10L, 2L);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.REJECTED);
    }

    @Test
    @DisplayName("호스트가 아닌 사람이 멤버 거절 시 예외 발생")
    void rejectMember_NotHost_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("not-host@test.com");

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.rejectMember(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACTION);
    }

    @Test
    @DisplayName("존재하지 않는 가입 신청 거절 시 예외 발생")
    void rejectMember_MemberNotFound_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.rejectMember(10L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_REQUEST_NOT_FOUND);
    }

    @Test
    @DisplayName("호스트가 자기 자신을 거절 시 예외 발생")
    void rejectMember_HostThemselves_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(host).status(MemberStatus.PENDING).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 1L)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.rejectMember(10L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SELF_ACTION_NOT_ALLOWED);
    }

    @Test
    @DisplayName("승인된 멤버 모임 탈퇴 성공 및 정원 상황에 따른 모임 오픈 복구")
    void leaveGathering_Success_ApprovedMember() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(2).status(GatheringStatus.CLOSED).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(guest).status(MemberStatus.APPROVED).build();

        given(securityService.getCurrentUser()).willReturn(guest);
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 2L)).willReturn(Optional.of(member));
        given(gatheringMemberRepository.countByGatheringIdAndStatus(10L, MemberStatus.APPROVED)).willReturn(1L);

        // when
        gatheringMemberService.leaveGathering(10L);

        // then
        verify(gatheringMemberRepository).delete(member);
        assertThat(gathering.getStatus()).isEqualTo(GatheringStatus.OPEN);
        assertThat(gathering.getCurrentJoining()).isEqualTo(1);
        verify(gatheringRepository).save(gathering);
    }

    @Test
    @DisplayName("대기 중인 멤버 모임 탈퇴 성공")
    void leaveGathering_Success_PendingMember() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(2).status(GatheringStatus.OPEN).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(guest).status(MemberStatus.PENDING).build();

        given(securityService.getCurrentUser()).willReturn(guest);
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 2L)).willReturn(Optional.of(member));

        // when
        gatheringMemberService.leaveGathering(10L);

        // then
        verify(gatheringMemberRepository).delete(member);
        verify(gatheringRepository, never()).save(any());
    }

    @Test
    @DisplayName("모임 멤버가 아닌 사람이 탈퇴 시도 시 예외 발생")
    void leaveGathering_NotMember_ThrowsException() {
        // given
        User stranger = User.builder().id(3L).email("stranger@test.com").build();
        given(securityService.getCurrentUser()).willReturn(stranger);
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 3L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.leaveGathering(10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACTION);
    }

    @Test
    @DisplayName("호스트가 탈퇴 시도 시 예외 발생")
    void leaveGathering_HostCannotLeave_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(host).status(MemberStatus.APPROVED).build();

        given(securityService.getCurrentUser()).willReturn(host);
        given(gatheringMemberRepository.findByGatheringIdAndUserId(10L, 1L)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.leaveGathering(10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACTION);
    }

    @Test
    @DisplayName("초대 멤버 등록 성공")
    void inviteMember_Success() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(5).currentJoining(1).members(new ArrayList<>()).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(userRepository.findById(2L)).willReturn(Optional.of(guest));

        // when
        gatheringMemberService.inviteMember(10L, 2L);

        // then
        assertThat(gathering.getMembers()).hasSize(1);
        assertThat(gathering.getMembers().get(0).getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(gathering.getCurrentJoining()).isEqualTo(1); // approved filter count
        verify(gatheringMemberRepository).save(any());
        verify(gatheringRepository).save(gathering);
    }

    @Test
    @DisplayName("호스트가 아닌 사람이 초대 시 예외 발생")
    void inviteMember_NotHost_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("not-host@test.com");

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.inviteMember(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACTION);
    }

    @Test
    @DisplayName("존재하지 않는 유저 초대 시 예외 발생")
    void inviteMember_UserNotFound_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.inviteMember(10L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 멤버로 가입된 유저 초대 시 예외 발생")
    void inviteMember_AlreadyMember_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).members(new ArrayList<>()).build();
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(guest).status(MemberStatus.APPROVED).build();
        gathering.getMembers().add(member);

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(userRepository.findById(2L)).willReturn(Optional.of(guest));

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.inviteMember(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("정원 초과 모임에 초대 시 예외 발생")
    void inviteMember_Full_ThrowsException() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(1).currentJoining(1).members(new ArrayList<>()).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(userRepository.findById(2L)).willReturn(Optional.of(guest));

        // when & then
        assertThatThrownBy(() -> gatheringMemberService.inviteMember(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("초대 완료 후 정원이 가득 찬 경우 모임 상태 CLOSED로 전환")
    void inviteMember_ClosedWhenFull() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        User guest = User.builder().id(2L).email("guest@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).maxJoining(1).currentJoining(0).members(new ArrayList<>()).build();

        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(securityService.getCurrentUserEmail()).willReturn("host@test.com");
        given(userRepository.findById(2L)).willReturn(Optional.of(guest));

        // when
        gatheringMemberService.inviteMember(10L, 2L);

        // then
        assertThat(gathering.getStatus()).isEqualTo(GatheringStatus.CLOSED);
        verify(gatheringRepository).save(gathering);
    }

    @Test
    @DisplayName("멤버 권한 확인 - 익명 로그인인 경우 false 반환")
    void isAuthorizedMember_AnonymousUser_ReturnsFalse() {
        // when & then
        assertThat(gatheringMemberService.isAuthorizedMember(10L, null)).isFalse();
        assertThat(gatheringMemberService.isAuthorizedMember(10L, "")).isFalse();
        assertThat(gatheringMemberService.isAuthorizedMember(10L, "anonymousUser")).isFalse();
    }

    @Test
    @DisplayName("멤버 권한 확인 - 모임 정보 미발견 시 false 반환")
    void isAuthorizedMember_GatheringNotFound_ReturnsFalse() {
        // given
        given(gatheringRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThat(gatheringMemberService.isAuthorizedMember(99L, "user@test.com")).isFalse();
    }

    @Test
    @DisplayName("멤버 권한 확인 - 호스트인 경우 true 반환")
    void isAuthorizedMember_Host_ReturnsTrue() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));

        // when & then
        assertThat(gatheringMemberService.isAuthorizedMember(10L, "host@test.com")).isTrue();
    }

    @Test
    @DisplayName("멤버 권한 확인 - 호스트가 없지만 연결된 여행 일정 작성자인 경우 true 반환")
    void isAuthorizedMember_HostNull_ItineraryAuthor_ReturnsTrue() {
        // given
        Itinerary itinerary = Itinerary.builder().id(1L).authorEmail("itinerary-author@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(null).linkedItinerary(itinerary).build();
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));

        // when & then
        assertThat(gatheringMemberService.isAuthorizedMember(10L, "itinerary-author@test.com")).isTrue();
    }

    @Test
    @DisplayName("멤버 권한 확인 - 승인된 멤버인 경우 true 반환")
    void isAuthorizedMember_ApprovedMember_ReturnsTrue() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(10L, "guest@test.com", MemberStatus.APPROVED)).willReturn(true);

        // when & then
        assertThat(gatheringMemberService.isAuthorizedMember(10L, "guest@test.com")).isTrue();
    }

    @Test
    @DisplayName("멤버 권한 확인 - 승인되지 않은 유저인 경우 false 반환")
    void isAuthorizedMember_NotApprovedMember_ReturnsFalse() {
        // given
        User host = User.builder().id(1L).email("host@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();
        given(gatheringRepository.findById(10L)).willReturn(Optional.of(gathering));
        given(gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(10L, "guest@test.com", MemberStatus.APPROVED)).willReturn(false);

        // when & then
        assertThat(gatheringMemberService.isAuthorizedMember(10L, "guest@test.com")).isFalse();
    }
}
