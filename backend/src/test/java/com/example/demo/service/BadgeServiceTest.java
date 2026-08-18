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

import static org.assertj.core.api.Assertions.assertThat;
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
}
