package com.example.demo.service;

import com.example.demo.domain.Stamp;
import com.example.demo.dto.StampResponse;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.StampRepository;
import com.example.demo.security.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StampServiceTest {

    @Mock
    private StampRepository stampRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private PointService pointService;

    @InjectMocks
    private StampService stampService;

    @Test
    @DisplayName("내 스탬프 목록 조회 성공")
    void getMyStamps_Success() {
        // given
        String email = "user@test.com";
        Stamp stamp = Stamp.builder().id(1L).title("Jeju Stamp").build();
        given(securityService.getCurrentUserEmail()).willReturn(email);
        given(stampRepository.findByUserEmailOrderByCompletedAtDesc(email)).willReturn(List.of(stamp));

        // when
        List<StampResponse> responses = stampService.getMyStamps();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getMissionTitle()).isEqualTo("Jeju Stamp");
    }

    @Test
    @DisplayName("스탬프 부여 성공 - PointService로 이전")
    void awardStamp_Success() {
        // when
        stampService.awardStamp(1L, 10L, "Busan Stamp", "stamp.png");

        // then
        verify(pointService).addPoints(1L, 0, 1, "Busan Stamp", 10L, "stamp.png");
    }

    @Test
    @DisplayName("스탬프 부여 시 유저 ID가 null인 경우 예외 발생")
    void awardStamp_NullUserId_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> stampService.awardStamp(null, 10L, "Busan Stamp", "stamp.png"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("스탬프를 부여할 유저 ID가 올바르지 않습니다.");
    }
}
