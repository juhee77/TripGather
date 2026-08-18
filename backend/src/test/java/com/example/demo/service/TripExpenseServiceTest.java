package com.example.demo.service;

import com.example.demo.domain.Trip;
import com.example.demo.domain.TripExpense;
import com.example.demo.domain.User;
import com.example.demo.dto.TripExpenseRequest;
import com.example.demo.dto.TripExpenseResponse;
import com.example.demo.dto.TripSettlementResponse;
import com.example.demo.repository.TripExpenseRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripExpenseServiceTest {

    @Mock
    private TripExpenseRepository tripExpenseRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripExpenseService tripExpenseService;

    @Test
    @DisplayName("지출 내역 등록 성공")
    void addExpense_Success() {
        // given
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).name("Hong").build();
        Trip trip = Trip.builder().id(10L).title("Jeju Trip").build();

        TripExpenseRequest request = TripExpenseRequest.builder()
                .tripId(10L)
                .title("Black Pork Dinner")
                .amount(new BigDecimal("90000"))
                .category("식비")
                .build();

        TripExpense savedExpense = TripExpense.builder()
                .id(100L)
                .trip(trip)
                .payer(user)
                .title("Black Pork Dinner")
                .amount(new BigDecimal("90000"))
                .category("식비")
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(tripRepository.findById(10L)).willReturn(Optional.of(trip));
        given(tripExpenseRepository.save(any(TripExpense.class))).willReturn(savedExpense);

        // when
        TripExpenseResponse response = tripExpenseService.addExpense(email, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Black Pork Dinner");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("90000"));
        assertThat(response.getPayerName()).isEqualTo("Hong");
    }

    @Test
    @DisplayName("N빵 정산 계산 성공")
    void calculateSettlement_Success() {
        // given
        Long tripId = 10L;
        User user1 = User.builder().id(1L).name("User 1").build();
        User user2 = User.builder().id(2L).name("User 2").build();

        TripExpense e1 = TripExpense.builder().id(1L).payer(user1).amount(new BigDecimal("60000")).build();
        TripExpense e2 = TripExpense.builder().id(2L).payer(user2).amount(new BigDecimal("30000")).build();

        given(tripExpenseRepository.findByTripIdOrderByExpenseDateDesc(tripId)).willReturn(List.of(e1, e2));

        // when (총 90,000원, 3명 정산 -> 1인당 30,000원)
        TripSettlementResponse response = tripExpenseService.calculateSettlement(tripId, 3);

        // then
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("90000"));
        assertThat(response.getPerPersonAmount()).isEqualByComparingTo(new BigDecimal("30000"));
        assertThat(response.getPayerSummaries()).hasSize(2);
    }
}
