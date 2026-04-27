package com.example.demo.service;

import com.example.demo.domain.PointTransaction;
import com.example.demo.domain.User;
import com.example.demo.repository.PointTransactionRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("포인트 추가 및 스탬프 적립 성공 테스트")
    void addPoints_Success() {
        // given
        User user = User.builder().id(1L).points(100).stampsCount(5).build();
        given(userRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(user));

        // when
        pointService.addPoints(1L, 50, 1, "출석 체크");

        // then
        assertThat(user.getPoints()).isEqualTo(150);
        assertThat(user.getStampsCount()).isEqualTo(6);
        verify(pointTransactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 포인트 추가 시 예외 발생")
    void addPoints_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.addPoints(1L, 50, 1, "출석 체크"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("포인트 차감 시 잔액이 부족하면 예외 발생")
    void addPoints_InsufficientBalance_ThrowsException() {
        // given
        User user = User.builder().id(1L).points(50).build();
        given(userRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> pointService.addPoints(1L, -100, 0, "포인트 사용"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("잔액이 부족합니다.");
    }
}
