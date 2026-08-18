package com.example.demo.dto;

import com.example.demo.domain.TripExpense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripExpenseResponse {
    private Long id;
    private Long tripId;
    private Long payerId;
    private String payerName;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDateTime expenseDate;
    private String memo;

    public static TripExpenseResponse from(TripExpense expense) {
        return TripExpenseResponse.builder()
                .id(expense.getId())
                .tripId(expense.getTrip().getId())
                .payerId(expense.getPayer().getId())
                .payerName(expense.getPayer().getName())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .memo(expense.getMemo())
                .build();
    }
}
