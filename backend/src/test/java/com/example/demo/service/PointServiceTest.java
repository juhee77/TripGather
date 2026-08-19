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
}
