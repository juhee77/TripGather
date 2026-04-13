package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;

import java.util.Optional;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatheringServiceImplTest {

    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @InjectMocks
    private GatheringServiceImpl gatheringService;

    private User host;
    private User user;
    private Gathering gathering;

    @BeforeEach
    void setUp() {
        host = User.builder().id(1L).email("host@example.com").name("Host").build();
        user = User.builder().id(2L).email("user@example.com").name("User").build();
        gathering = Gathering.builder()
                .id(1L)
                .title("Test Gathering")
                .host(host)
                .maxJoining(5)
                .members(new ArrayList<>())
                .build();
        
        // Setup SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("모임 생성 성공")
    void createGathering_Success() {
        // given
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        given(auth.getName()).willReturn("host@example.com");
        given(userRepository.findByEmail("host@example.com")).willReturn(Optional.of(host));
        given(gatheringRepository.save(any(Gathering.class))).willReturn(gathering);

        // when
        Gathering result = gatheringService.createGathering(gathering);

        // then
        assertThat(result.getHost()).isEqualTo(host);
        verify(gatheringRepository, atLeastOnce()).save(any(Gathering.class));
        verify(gatheringMemberRepository).save(any(GatheringMember.class));
    }

    @Test
    @DisplayName("모임 참가 신청 성공")
    void joinGathering_Success() {
        // given
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        given(auth.getName()).willReturn("user@example.com");
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(gatheringRepository.save(any(Gathering.class))).willReturn(gathering);

        // when
        Gathering result = gatheringService.joinGathering(1L);

        // then
        assertThat(result).isNotNull();
        verify(gatheringMemberRepository).save(any(GatheringMember.class));
    }

    @Test
    @DisplayName("모임 정원 초과 시 참가 신청 실패")
    void joinGathering_CapacityExceeded_ThrowsException() {
        // given
        gathering.setMaxJoining(0); // Force capacity check failure or just manually add members
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        given(auth.getName()).willReturn("user@example.com");
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> gatheringService.joinGathering(1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("모임 정원이 초과되었습니다.");
    }

    @Test
    @DisplayName("모임 멤버 승인 성공")
    void approveMember_Success() {
        // given
        GatheringMember member = GatheringMember.builder()
                .gathering(gathering)
                .user(user)
                .status(MemberStatus.PENDING)
                .build();
        gathering.getMembers().add(member);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        given(auth.getName()).willReturn("host@example.com");
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));
        given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 2L)).willReturn(Optional.of(member));

        // when
        gatheringService.approveMember(1L, 2L);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
    }
}
