package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Test
    @DisplayName("모임 전체 조회 - 지역 파라미터가 없거나 전체인 경우")
    void getAllGatherings_withoutLocation() {
        // given
        Gathering gathering = new Gathering();
        given(gatheringRepository.searchGatherings(isNull(), isNull(), isNull())).willReturn(List.of(gathering));

        // when
        List<Gathering> result1 = gatheringService.getAllGatherings(null);
        List<Gathering> result2 = gatheringService.getAllGatherings("전체");
        List<Gathering> result3 = gatheringService.getAllGatherings("");

        // then
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);
        assertThat(result3).hasSize(1);
        verify(gatheringRepository, times(3)).searchGatherings(isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("모임 지역 필터링 조회")
    void getAllGatherings_withLocation() {
        // given
        String location = "강남구";
        Gathering gathering = new Gathering();
        given(gatheringRepository.searchGatherings(isNull(), isNull(), eq(location))).willReturn(List.of(gathering));

        // when
        List<Gathering> result = gatheringService.getAllGatherings(location);

        // then
        assertThat(result).hasSize(1);
        verify(gatheringRepository).searchGatherings(isNull(), isNull(), eq(location));
    }

    @Test
    @DisplayName("모임 참가 신청 처리 성공")
    void joinGathering_Success() {
        // given
        Long gatheringId = 1L;
        String userEmail = "test@test.com";

        mockSecurityContext(userEmail);

        Gathering gathering = new Gathering();
        gathering.setMaxJoining(5);
        
        User user = new User();
        user.setEmail(userEmail);
        user.setId(2L);
        
        User host = new User();
        host.setEmail("host@test.com");
        host.setId(1L);
        gathering.setHost(host);

        given(gatheringRepository.findById(gatheringId)).willReturn(Optional.of(gathering));
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(gatheringRepository.save(any(Gathering.class))).willAnswer(i -> i.getArgument(0));

        // when
        Gathering result = gatheringService.joinGathering(gatheringId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("모임 생성 성공 - 호스트 자동 멤버 등록 확인")
    void createGathering_Success() {
        // given
        String email = "host@test.com";
        mockSecurityContext(email);

        User host = new User();
        host.setEmail(email);
        host.setId(1L);

        Gathering gathering = new Gathering();
        gathering.setTitle("테스트 모임");
        gathering.setMembers(new java.util.ArrayList<>());

        given(userRepository.findByEmail(email)).willReturn(Optional.of(host));
        given(gatheringRepository.save(any(Gathering.class))).willAnswer(i -> i.getArgument(0));

        // when
        Gathering result = gatheringService.createGathering(gathering);

        // then
        assertThat(result.getHost()).isEqualTo(host);
        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(result.getCurrentJoining()).isEqualTo(1);
        verify(gatheringMemberRepository).save(any(GatheringMember.class));
    }

    @Test
    @DisplayName("멤버 승인 성공 - 인원수 업데이트 확인")
    void approveMember_Success() {
        // given
        Long gatheringId = 1L;
        Long userId = 2L;
        String hostEmail = "host@test.com";

        mockSecurityContext(hostEmail);

        User host = new User();
        host.setEmail(hostEmail);
        host.setId(1L);

        Gathering gathering = new Gathering();
        gathering.setId(gatheringId);
        gathering.setHost(host);
        gathering.setMembers(new java.util.ArrayList<>());

        User applicant = new User();
        applicant.setId(userId);

        GatheringMember member = GatheringMember.builder()
                .gathering(gathering)
                .user(applicant)
                .status(MemberStatus.PENDING)
                .build();
        gathering.getMembers().add(member);

        given(gatheringRepository.findById(gatheringId)).willReturn(Optional.of(gathering));
        given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId)).willReturn(Optional.of(member));

        // when
        gatheringService.approveMember(gatheringId, userId);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(gathering.getCurrentJoining()).isEqualTo(1);
    }

    @Test
    @DisplayName("모임 탈퇴 성공 - 인원수 재계산 확인")
    void leaveGathering_Success() {
        // given
        Long gatheringId = 1L;
        String userEmail = "member@test.com";

        mockSecurityContext(userEmail);

        User user = new User();
        user.setEmail(userEmail);
        user.setId(2L);

        User host = new User();
        host.setId(1L);
        host.setEmail("host@test.com");

        Gathering gathering = new Gathering();
        gathering.setId(gatheringId);
        gathering.setHost(host);

        GatheringMember member = GatheringMember.builder()
                .gathering(gathering)
                .user(user)
                .status(MemberStatus.APPROVED)
                .build();

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, user.getId())).willReturn(Optional.of(member));
        given(gatheringMemberRepository.countByGatheringIdAndStatus(gatheringId, MemberStatus.APPROVED)).willReturn(0L);

        // when
        gatheringService.leaveGathering(gatheringId);

        // then
        verify(gatheringMemberRepository).delete(member);
        assertThat(gathering.getCurrentJoining()).isEqualTo(0);
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }
}
