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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
}
