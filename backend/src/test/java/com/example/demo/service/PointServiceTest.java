package com.example.demo.service;

import com.example.demo.domain.PointTransaction;
import com.example.demo.domain.User;
import com.example.demo.dto.PointTransactionResponse;
import com.example.demo.repository.PointTransactionRepository;
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

import com.example.demo.domain.Stamp;
import com.example.demo.exception.CustomException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private StampRepository stampRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("유저 포인트 거래 내역 최신순 조회 성공")
    void getUserPointTransactions_Success() {
        // given
        String email = "test@example.com";
        User user = User.builder().id(100L).email(email).points(100).build();
        PointTransaction tx1 = PointTransaction.of(user, 20, "체크인 완료");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(100L)).willReturn(List.of(tx1));

        // when
        List<PointTransactionResponse> result = pointService.getUserPointTransactions(email);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(20);
        assertThat(result.get(0).getDescription()).isEqualTo("체크인 완료");
    }

    @Test
    @DisplayName("유저 포인트 거래 내역 유형(EARN/USE) 동적 필터링 조회 성공")
    void getUserPointTransactions_WithTypeFilter_Success() {
        // given
        String email = "test@example.com";
        User user = User.builder().id(100L).email(email).points(100).build();
        PointTransaction txEarn = PointTransaction.of(user, 50, "스탠바이 체크인");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(pointTransactionRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(100L, "EARN"))
                .willReturn(List.of(txEarn));

        // when
        List<PointTransactionResponse> result = pointService.getUserPointTransactions(email, "EARN");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(50);
        assertThat(result.get(0).getTransactionType()).isEqualTo("EARN");
    }

    @Test
    @DisplayName("포인트 및 스탬프 적립/차감 금액이 모두 0일 때 예외 발생")
    void addPoints_ZeroAmountAndStamp_ThrowsException() {
        // given
        User user = User.builder().id(100L).points(100).build();
        given(userRepository.findByIdWithPessimisticLock(100L)).willReturn(Optional.of(user));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pointService.addPoints(100L, 0, 0, "유효하지 않은 트랜잭션"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("적립 또는 차감할 포인트/스탬프를 입력해주세요.");
    }

    @Test
    @DisplayName("포인트 차감 시 잔여 포인트 부족할 때 예외 발생")
    void addPoints_InsufficientPoints_ThrowsException() {
        // given
        User user = User.builder().id(100L).points(50).build();
        given(userRepository.findByIdWithPessimisticLock(100L)).willReturn(Optional.of(user));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pointService.addPoints(100L, -100, 0, "과도한 포인트 차감"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("포인트 적립 성공 시 잔액이 증가하고 거래 내역이 저장된다")
    void addPoints_Success() {
        // given
        User user = User.builder().id(1L).email("test@example.com").points(100).stampsCount(0).build();
        given(userRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(user));

        // when
        pointService.addPoints(1L, 50, 0, "체크인 완료");

        // then
        assertThat(user.getPoints()).isEqualTo(150);
        assertThat(user.getStampsCount()).isZero();
        verify(pointTransactionRepository).save(any(PointTransaction.class));
        verify(stampRepository, never()).save(any(Stamp.class));
    }

    @Test
    @DisplayName("스탬프를 함께 적립하면 스탬프 수가 늘고 스탬프가 저장된다")
    void addPoints_WithStamp_SavesStamp() {
        // given
        User user = User.builder().id(1L).email("test@example.com").points(0).stampsCount(2).build();
        given(userRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(user));

        // when
        pointService.addPoints(1L, 30, 1, "여정 완주", 77L, "https://cdn/stamp.png");

        // then
        assertThat(user.getPoints()).isEqualTo(30);
        assertThat(user.getStampsCount()).isEqualTo(3);
        verify(stampRepository).save(any(Stamp.class));
        verify(pointTransactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("잔액과 정확히 같은 금액은 차감할 수 있다")
    void addPoints_ExactBalance_Succeeds() {
        // given
        User user = User.builder().id(1L).email("test@example.com").points(50).stampsCount(0).build();
        given(userRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(user));

        // when
        pointService.addPoints(1L, -50, 0, "포인트 전액 사용");

        // then
        assertThat(user.getPoints()).isZero();
        verify(pointTransactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자에게 포인트 적립 시 예외 발생")
    void addPoints_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByIdWithPessimisticLock(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.addPoints(999L, 10, 0, "적립"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 포인트 내역 조회 시 예외 발생")
    void getUserPointTransactions_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail("ghost@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.getUserPointTransactions("ghost@example.com"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
}
