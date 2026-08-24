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
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "지출 금액은 0원보다 커야 합니다.");
        }

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

    @Transactional
    public TripExpenseResponse updateExpense(Long expenseId, String userEmail, TripExpenseRequest request) {
        TripExpense expense = tripExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "지출 내역을 찾을 수 없습니다: " + expenseId));

        if (!expense.getPayer().getEmail().equals(userEmail)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "지출 등록자만 수정할 수 있습니다.");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "지출 금액은 0원보다 커야 합니다.");
        }

        if (request.getTitle() != null) {
            profanityFilterService.validateText(request.getTitle());
            expense.setTitle(request.getTitle().trim());
        }
        if (request.getMemo() != null) {
            profanityFilterService.validateText(request.getMemo());
            expense.setMemo(request.getMemo());
        }

        expense.setAmount(request.getAmount());
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory().trim());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }

        return TripExpenseResponse.from(tripExpenseRepository.save(expense));
    }

    public List<TripExpenseResponse> getExpensesByTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다: " + tripId);
        }
        return tripExpenseRepository.findByTripIdOrderByExpenseDateDesc(tripId).stream()
                .map(TripExpenseResponse::from)
                .collect(Collectors.toList());
    }

    public TripSettlementResponse calculateSettlement(Long tripId, int memberCount) {
        if (!tripRepository.existsById(tripId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다: " + tripId);
        }
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
