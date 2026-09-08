package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.BadgeDto;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.StampRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import com.example.demo.domain.Gathering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private StampRepository stampRepository;

    @InjectMocks
    private BadgeService badgeService;

    @Test
    @DisplayName("유저 뱃지 조회 성공 및 획득 여부 계산 검증")
    void getUserBadges_Success() {
        // given
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).points(1500).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(gatheringRepository.findAll()).willReturn(List.of());
        given(stampRepository.countByUserId(1L)).willReturn(2L);

        // when
        List<BadgeDto> badges = badgeService.getUserBadges(email);

        // then
        assertThat(badges).hasSize(4);
        assertThat(badges.get(0).isUnlocked()).isTrue(); // 첫 발걸음
        assertThat(badges.get(1).isUnlocked()).isFalse(); // 열정적인 호스트 (0개)
        assertThat(badges.get(2).isUnlocked()).isTrue(); // 스탬프 수집가 (2개)
        assertThat(badges.get(3).isUnlocked()).isTrue(); // 포인트 리치 (1500pt)
    }

    private BadgeDto badgeOf(List<BadgeDto> badges, String code) {
        return badges.stream().filter(b -> b.getCode().equals(code)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("조건을 하나도 만족하지 않으면 기본 뱃지만 획득 상태")
    void getUserBadges_NothingAchieved_OnlyBeginnerUnlocked() {
        // given
        String email = "new@example.com";
        User user = User.builder().id(1L).email(email).points(0).build();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(gatheringRepository.findAll()).willReturn(List.of());
        given(stampRepository.countByUserId(1L)).willReturn(0L);

        // when
        List<BadgeDto> badges = badgeService.getUserBadges(email);

        // then
        assertThat(badgeOf(badges, "BEGINNER_EXPLORER").isUnlocked()).isTrue();
        assertThat(badgeOf(badges, "PASSIONATE_HOST").isUnlocked()).isFalse();
        assertThat(badgeOf(badges, "STAMP_COLLECTOR").isUnlocked()).isFalse();
        assertThat(badgeOf(badges, "POINT_RICH").isUnlocked()).isFalse();
    }

    @Test
    @DisplayName("모든 조건을 만족하면 전체 뱃지 획득 상태")
    void getUserBadges_AllAchieved_AllUnlocked() {
        // given
        String email = "veteran@example.com";
        User user = User.builder().id(1L).email(email).points(1000).build();
        Gathering hosted = Gathering.builder().id(1L).host(user).build();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(gatheringRepository.findAll()).willReturn(List.of(hosted));
        given(stampRepository.countByUserId(1L)).willReturn(1L);

        // when
        List<BadgeDto> badges = badgeService.getUserBadges(email);

        // then
        assertThat(badges).allMatch(BadgeDto::isUnlocked);
    }

    @Test
    @DisplayName("호스트가 없거나 타인이 호스팅한 모임은 호스트 뱃지 조건에서 제외")
    void getUserBadges_OtherHostedGatherings_HostBadgeLocked() {
        // given
        String email = "member@example.com";
        User user = User.builder().id(1L).email(email).points(0).build();
        User otherHost = User.builder().id(2L).email("host@example.com").build();
        Gathering hostless = Gathering.builder().id(1L).host(null).build();
        Gathering othersGathering = Gathering.builder().id(2L).host(otherHost).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(gatheringRepository.findAll()).willReturn(List.of(hostless, othersGathering));
        given(stampRepository.countByUserId(1L)).willReturn(0L);

        // when
        List<BadgeDto> badges = badgeService.getUserBadges(email);

        // then
        assertThat(badgeOf(badges, "PASSIONATE_HOST").isUnlocked()).isFalse();
    }

    @Test
    @DisplayName("포인트가 정확히 1,000이면 포인트 뱃지 획득")
    void getUserBadges_ExactlyThousandPoints_PointBadgeUnlocked() {
        // given
        String email = "rich@example.com";
        User user = User.builder().id(1L).email(email).points(1000).build();
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(gatheringRepository.findAll()).willReturn(List.of());
        given(stampRepository.countByUserId(1L)).willReturn(0L);

        // when
        List<BadgeDto> badges = badgeService.getUserBadges(email);

        // then
        assertThat(badgeOf(badges, "POINT_RICH").isUnlocked()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 유저의 뱃지 조회 시 예외 발생")
    void getUserBadges_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail("ghost@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> badgeService.getUserBadges("ghost@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
