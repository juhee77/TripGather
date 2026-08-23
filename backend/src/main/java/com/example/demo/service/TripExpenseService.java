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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripExpenseService {

    private final TripExpenseRepository tripExpenseRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final ProfanityFilterService profanityFilterService;

    @Transactional
    public TripExpenseResponse addExpense(String userEmail, TripExpenseRequest request) {
        if (request.getTitle() != null) {
            profanityFilterService.validateText(request.getTitle());
        }
        if (request.getMemo() != null) {
            profanityFilterService.validateText(request.getMemo());
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다: " + request.getTripId()));

        TripExpense expense = TripExpense.builder()
                .trip(trip)
                .payer(user)
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(request.getCategory() != null ? request.getCategory() : "기타")
                .expenseDate(request.getExpenseDate())
                .memo(request.getMemo())
                .build();

        TripExpense saved = tripExpenseRepository.save(expense);
        return TripExpenseResponse.from(saved);
    }

    @Transactional
    public void deleteExpense(Long expenseId, String userEmail) {
        TripExpense expense = tripExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new com.example.demo.exception.CustomException(
                        com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "지출 내역을 찾을 수 없습니다: " + expenseId));

        if (!expense.getPayer().getEmail().equals(userEmail)) {
            throw new com.example.demo.exception.CustomException(
                    com.example.demo.exception.ErrorCode.FORBIDDEN_ACTION, "지출 등록자만 삭제할 수 있습니다.");
        }

        tripExpenseRepository.delete(expense);
    }

    public List<TripExpenseResponse> getExpensesByTrip(Long tripId) {
        return tripExpenseRepository.findByTripIdOrderByExpenseDateDesc(tripId).stream()
                .map(TripExpenseResponse::from)
                .collect(Collectors.toList());
    }

    public TripSettlementResponse calculateSettlement(Long tripId, int memberCount) {
        if (memberCount <= 0) {
            memberCount = 1;
        }

        List<TripExpense> expenses = tripExpenseRepository.findByTripIdOrderByExpenseDateDesc(tripId);

        BigDecimal totalAmount = expenses.stream()
                .map(TripExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal perPersonAmount = expenses.isEmpty() ? BigDecimal.ZERO : totalAmount.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);

        Map<User, BigDecimal> paidMap = new HashMap<>();
        for (TripExpense expense : expenses) {
            paidMap.merge(expense.getPayer(), expense.getAmount(), BigDecimal::add);
        }

        List<TripSettlementResponse.PayerSummary> summaries = paidMap.entrySet().stream()
                .map(entry -> {
                    User payer = entry.getKey();
                    BigDecimal totalPaid = entry.getValue();
                    BigDecimal balance = totalPaid.subtract(perPersonAmount);
                    return TripSettlementResponse.PayerSummary.builder()
                            .userId(payer.getId())
                            .userName(payer.getName())
                            .totalPaid(totalPaid)
                            .balance(balance)
                            .build();
                })
                .collect(Collectors.toList());

        return TripSettlementResponse.builder()
                .tripId(tripId)
                .totalAmount(totalAmount)
                .memberCount(memberCount)
                .perPersonAmount(perPersonAmount)
                .payerSummaries(summaries)
                .build();
    }
}
