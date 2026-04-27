package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
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
}
