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
import static org.mockito.ArgumentMatchers.anyString;
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
    @Mock
    private com.example.demo.repository.GatheringLikeRepository gatheringLikeRepository;

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
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
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

    @Test
    @DisplayName("모임 좋아요/취소 성공")
    void likeGathering_Success() {
        // given
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        given(auth.getName()).willReturn("user@example.com");
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));
        given(gatheringLikeRepository.findByUserAndGathering(user, gathering)).willReturn(Optional.empty());

        // when (Like)
        gatheringService.likeGathering(1L);

        // then
        assertThat(gathering.getLikeCount()).isEqualTo(1);
        verify(gatheringLikeRepository).save(any());

        // given (Unlike)
        given(gatheringLikeRepository.findByUserAndGathering(user, gathering))
                .willReturn(Optional.of(com.example.demo.domain.GatheringLike.builder().user(user).gathering(gathering).build()));

        // when
        gatheringService.likeGathering(1L);

        // then
        assertThat(gathering.getLikeCount()).isEqualTo(0);
        verify(gatheringLikeRepository).delete(any());
    }

    @Test
    @DisplayName("프라이버시 설정 포함 모임 업데이트 성공")
    void updateGathering_WithPrivacy_Success() {
        // given
        Gathering updateData = Gathering.builder()
                .title("Updated Title")
                .location("New Location")
                .dates("2024-12-31")
                .maxJoining(10)
                .isGalleryPublic(true)
                .isChatPublic(true)
                .isCommentPublic(false)
                .build();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        given(auth.getName()).willReturn("host@example.com");
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));

        // when
        Gathering result = gatheringService.updateGathering(1L, updateData);

        // then
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.isGalleryPublic()).isTrue();
        assertThat(result.isChatPublic()).isTrue();
        assertThat(result.isCommentPublic()).isFalse();
    }

    @Test
    @DisplayName("좋아요 여부 확인 성공")
    void isLikedByUser_Success() {
        // given
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));
        given(gatheringLikeRepository.existsByUserAndGathering(user, gathering)).willReturn(true);

        // when
        boolean result = gatheringService.isLikedByUser(1L, "user@example.com");

        // then
        assertThat(result).isTrue();

        // when (Anonymous)
        assertThat(gatheringService.isLikedByUser(1L, "anonymousUser")).isFalse();
    }

    @Test
    @DisplayName("모임 삭제 성공")
    void deleteGathering_Success() {
        // given
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        lenient().when(auth.getName()).thenReturn("host@example.com");
        lenient().when(userRepository.findByEmail("host@example.com")).thenReturn(Optional.of(host));
        lenient().when(gatheringRepository.findById(1L)).thenReturn(Optional.of(gathering));

        // when
        gatheringService.deleteGathering(1L);

        // then
        verify(gatheringRepository).deleteById(1L);
    }

    @Test
    @DisplayName("모임 탈퇴 성공")
    void leaveGathering_Success() {
        // given
        GatheringMember member = GatheringMember.builder()
                .gathering(gathering)
                .user(user)
                .status(MemberStatus.APPROVED)
                .build();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        lenient().when(auth.getName()).thenReturn("user@example.com");
        lenient().when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        lenient().when(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 2L)).thenReturn(Optional.of(member));

        // when
        gatheringService.leaveGathering(1L);

        // then
        verify(gatheringMemberRepository).delete(member);
    }

    @Test
    @DisplayName("멤버 거절 성공")
    void rejectMember_Success() {
        // given
        GatheringMember member = GatheringMember.builder().gathering(gathering).user(user).status(MemberStatus.PENDING).build();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        lenient().when(auth.getName()).thenReturn("host@example.com");
        lenient().when(gatheringRepository.findById(1L)).thenReturn(Optional.of(gathering));
        lenient().when(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 2L)).thenReturn(Optional.of(member));

        // when
        gatheringService.rejectMember(1L, 2L);

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.REJECTED);
    }

    @Test
    @DisplayName("멤버 초대 성공")
    void inviteMember_Success() {
        // given
        User target = User.builder().id(3L).email("target@example.com").build();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        lenient().when(auth.getName()).thenReturn("host@example.com");
        lenient().when(gatheringRepository.findById(1L)).thenReturn(Optional.of(gathering));
        lenient().when(userRepository.findById(3L)).thenReturn(Optional.of(target));

        // when
        gatheringService.inviteMember(1L, 3L);

        // then
        verify(gatheringMemberRepository).save(any(GatheringMember.class));
    }

    @Test
    @DisplayName("전체 모임 조회 성공")
    void getAllGatherings_Success() {
        // given
        java.util.List<Gathering> list = java.util.List.of(gathering);
        lenient().when(gatheringRepository.searchGatherings(isNull(), isNull(), isNull(), isNull())).thenReturn(list);
        lenient().when(gatheringRepository.searchGatherings(any(), any(), any(), any())).thenReturn(list);

        // when
        java.util.List<Gathering> results = gatheringService.getAllGatherings(null);

        // then
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).isEqualTo("Test Gathering");
    }

    @Test
    @DisplayName("모임 검색 성공")
    void searchGatherings_Success() {
        // given
        given(gatheringRepository.searchGatherings(any(), any(), any(), any())).willReturn(java.util.List.of(gathering));

        // when
        java.util.List<Gathering> results = gatheringService.searchGatherings("Query", "Category", "Location", true);

        // then
        assertThat(results).hasSize(1);
    }
}
