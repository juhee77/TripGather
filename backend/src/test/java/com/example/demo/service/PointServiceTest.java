package com.example.demo.service;

import com.example.demo.domain.PointTransaction;
import com.example.demo.domain.Stamp;
import com.example.demo.domain.User;
import com.example.demo.dto.PointTransactionResponse;
import com.example.demo.repository.PointTransactionRepository;
import com.example.demo.repository.StampRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @DisplayName("포인트만 적립 시 유저 포인트 증가 및 거래 내역 저장, 스탬프는 미발급")
    void addPoints_PointsOnly_Success() {
        // given
        User user = User.builder().id(100L).points(50).stampsCount(1).build();
        given(userRepository.findByIdWithPessimisticLock(100L)).willReturn(Optional.of(user));
        ArgumentCaptor<PointTransaction> txCaptor = ArgumentCaptor.forClass(PointTransaction.class);

        // when
        pointService.addPoints(100L, 100, 0, "여행 후기 작성");

        // then
        assertThat(user.getPoints()).isEqualTo(150);
        assertThat(user.getStampsCount()).isEqualTo(1);
        verify(stampRepository, never()).save(org.mockito.ArgumentMatchers.any(Stamp.class));
        verify(pointTransactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getAmount()).isEqualTo(100);
        assertThat(txCaptor.getValue().getDescription()).isEqualTo("여행 후기 작성");
    }

    @Test
    @DisplayName("스탬프 적립 시 스탬프 카운트 증가 및 스탬프 엔티티 저장")
    void addPoints_WithStamp_SavesStamp() {
        // given
        User user = User.builder().id(100L).points(0).stampsCount(2).build();
        given(userRepository.findByIdWithPessimisticLock(100L)).willReturn(Optional.of(user));
        ArgumentCaptor<Stamp> stampCaptor = ArgumentCaptor.forClass(Stamp.class);

        // when
        pointService.addPoints(100L, 0, 1, "제주 스탬프", 7L, "stamp.png");

        // then
        assertThat(user.getStampsCount()).isEqualTo(3);
        verify(stampRepository).save(stampCaptor.capture());
        Stamp saved = stampCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getGatheringId()).isEqualTo(7L);
        assertThat(saved.getTitle()).isEqualTo("제주 스탬프");
        assertThat(saved.getStampImageUrl()).isEqualTo("stamp.png");
        verify(pointTransactionRepository).save(org.mockito.ArgumentMatchers.any(PointTransaction.class));
    }

    @Test
    @DisplayName("포인트 차감 시 잔액이 정확히 0이 되는 경우 정상 처리")
    void addPoints_ExactBalanceDeduction_Success() {
        // given
        User user = User.builder().id(100L).points(100).build();
        given(userRepository.findByIdWithPessimisticLock(100L)).willReturn(Optional.of(user));

        // when
        pointService.addPoints(100L, -100, 0, "포인트 전액 사용");

        // then
        assertThat(user.getPoints()).isZero();
        verify(pointTransactionRepository).save(org.mockito.ArgumentMatchers.any(PointTransaction.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 포인트 적립 시 예외 발생")
    void addPoints_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByIdWithPessimisticLock(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.addPoints(999L, 100, 0, "포인트 적립"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("유저 포인트 거래 내역 USE 유형 필터링 조회 성공")
    void getUserPointTransactions_UseTypeFilter_Success() {
        // given
        String email = "test@example.com";
        User user = User.builder().id(100L).email(email).points(100).build();
        PointTransaction txUse = PointTransaction.of(user, -30, "포인트 사용");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(pointTransactionRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(100L, "USE"))
                .willReturn(List.of(txUse));

        // when
        List<PointTransactionResponse> result = pointService.getUserPointTransactions(email, "USE");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(-30);
        assertThat(result.get(0).getTransactionType()).isEqualTo("USE");
    }

    @Test
    @DisplayName("존재하지 않는 유저 이메일로 포인트 거래 내역 조회 시 예외 발생")
    void getUserPointTransactions_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.getUserPointTransactions("unknown@example.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
}
