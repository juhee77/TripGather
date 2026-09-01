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

    @Mock
    private ProfanityFilterService profanityFilterService;

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
        given(tripRepository.existsById(tripId)).willReturn(true);
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

    @Test
    @DisplayName("지출 내역이 비어 있을 때 정산 결과 0원 반환")
    void calculateSettlement_EmptyExpenses_ReturnsZeroSettlement() {
        // given
        Long tripId = 10L;
        given(tripRepository.existsById(tripId)).willReturn(true);
        given(tripExpenseRepository.findByTripIdOrderByExpenseDateDesc(tripId)).willReturn(List.of());

        // when
        TripSettlementResponse response = tripExpenseService.calculateSettlement(tripId, 2);

        // then
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getPerPersonAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getPayerSummaries()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 정산 계산 시 예외 발생")
    void calculateSettlement_TripNotFound_ThrowsException() {
        // given
        Long tripId = 99L;
        given(tripRepository.existsById(tripId)).willReturn(false);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.calculateSettlement(tripId, 2))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("지출 내역 등록 시 항목명에 비속어 포함 시 예외 발생")
    void addExpense_ProfanityTitle_ThrowsException() {
        // given
        String email = "test@example.com";
        TripExpenseRequest request = TripExpenseRequest.builder()
                .tripId(10L)
                .title("개새끼 식당 식사")
                .amount(new BigDecimal("50000"))
                .build();

        org.mockito.BDDMockito.willThrow(new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText("개새끼 식당 식사");

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.addExpense(email, request))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("지출 내역 삭제 성공 - 작성자 본인")
    void deleteExpense_Success() {
        // given
        Long expenseId = 100L;
        String email = "owner@test.com";
        User user = User.builder().id(1L).email(email).build();
        TripExpense expense = TripExpense.builder().id(expenseId).payer(user).build();

        given(tripExpenseRepository.findById(expenseId)).willReturn(Optional.of(expense));

        // when
        tripExpenseService.deleteExpense(expenseId, email);

        // then
        verify(tripExpenseRepository).delete(expense);
    }

    @Test
    @DisplayName("지출 내역 삭제 실패 - 타인 작성 지출 삭제 시 예외 발생")
    void deleteExpense_AccessDenied_ThrowsException() {
        // given
        Long expenseId = 100L;
        String ownerEmail = "owner@test.com";
        String attackerEmail = "attacker@test.com";
        User user = User.builder().id(1L).email(ownerEmail).build();
        TripExpense expense = TripExpense.builder().id(expenseId).payer(user).build();

        given(tripExpenseRepository.findById(expenseId)).willReturn(Optional.of(expense));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripExpenseService.deleteExpense(expenseId, attackerEmail))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("지출 내역 등록 시 0원 이하 금액 제출 시 예외 발생")
    void addExpense_ZeroAmount_ThrowsException() {
        // given
        TripExpenseRequest request = TripExpenseRequest.builder()
                .tripId(1L)
                .amount(java.math.BigDecimal.ZERO)
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.addExpense("user@test.com", request))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("지출 금액은 0원보다 커야 합니다.");
    }

    @Test
    @DisplayName("지출 내역 수정 시 0원 이하 금액 제출 시 예외 발생")
    void updateExpense_ZeroAmount_ThrowsException() {
        // given
        Long expenseId = 10L;
        String email = "owner@test.com";
        User user = User.builder().id(1L).email(email).build();
        TripExpense expense = TripExpense.builder().id(expenseId).payer(user).build();

        given(tripExpenseRepository.findById(expenseId)).willReturn(Optional.of(expense));

        TripExpenseRequest request = TripExpenseRequest.builder()
                .amount(java.math.BigDecimal.ZERO)
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.updateExpense(expenseId, email, request))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("지출 금액은 0원보다 커야 합니다.");
    }

    @Test
    @DisplayName("여행별 지출 목록 조회 성공")
    void getExpensesByTrip_Success() {
        // given
        Long tripId = 10L;
        given(tripRepository.existsById(tripId)).willReturn(true);
        given(tripExpenseRepository.findByTripIdOrderByExpenseDateDesc(tripId)).willReturn(List.of());

        // when
        List<TripExpenseResponse> result = tripExpenseService.getExpensesByTrip(tripId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 지출 목록 조회 시 예외 발생")
    void getExpensesByTrip_TripNotFound_ThrowsException() {
        // given
        Long tripId = 99L;
        given(tripRepository.existsById(tripId)).willReturn(false);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.getExpensesByTrip(tripId))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("N빵 정산 계산 시 0명 이하의 인원 전달 시 예외 발생")
    void calculateSettlement_InvalidMemberCount_ThrowsException() {
        // given
        Long tripId = 10L;
        given(tripRepository.existsById(tripId)).willReturn(true);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.calculateSettlement(tripId, 0))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("정산 인원은 최소 1명 이상이어야 합니다.");
    }

    @Test
    @DisplayName("지출 내역 수정 성공 테스트")
    void updateExpense_Success() {
        // given
        Long expenseId = 10L;
        String email = "owner@test.com";
        User user = User.builder().id(1L).email(email).name("Hong").build();
        Trip trip = Trip.builder().id(100L).title("Jeju").build();
        TripExpense expense = TripExpense.builder().id(expenseId).payer(user).trip(trip).amount(new BigDecimal("50000")).title("점심").build();

        given(tripExpenseRepository.findById(expenseId)).willReturn(Optional.of(expense));
        given(tripExpenseRepository.save(any(TripExpense.class))).willReturn(expense);

        TripExpenseRequest request = TripExpenseRequest.builder()
                .title("저녁 식사")
                .amount(new BigDecimal("70000"))
                .category("식비")
                .build();

        // when
        TripExpenseResponse response = tripExpenseService.updateExpense(expenseId, email, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("저녁 식사");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("70000"));
    }

    @Test
    @DisplayName("지출 내역 등록 시 0원 이하 금액 지정 시 예외 발생")
    void addExpense_InvalidAmount_ThrowsException() {
        // given
        String email = "test@example.com";
        TripExpenseRequest request = TripExpenseRequest.builder()
                .tripId(10L)
                .title("Zero Expense")
                .amount(new BigDecimal("0"))
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.addExpense(email, request))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("지출 금액은 0원보다 커야 합니다.");
    }

    @Test
    @DisplayName("지출 내역 수정 시 null 지출 ID 전달 시 예외 발생")
    void updateExpense_NullExpenseIdOrEmail_ThrowsException() {
        // given
        TripExpenseRequest request = TripExpenseRequest.builder().title("수정").build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.updateExpense(null, "user@test.com", request))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("지출 ID 또는 유저 정보가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("지출 내역 삭제 시 null 지출 ID 전달 시 예외 발생")
    void deleteExpense_NullExpenseIdOrEmail_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.deleteExpense(null, "user@test.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("지출 ID 또는 유저 정보가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("N빵 정산 계산 시 존재하지 않는 여행 ID 전달 시 예외 발생")
    void calculateSettlement_NonExistingTrip_ThrowsException() {
        // given
        Long tripId = 999L;
        given(tripRepository.existsById(tripId)).willReturn(false);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripExpenseService.calculateSettlement(tripId, 3))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다: 999");
    }
}
