package com.example.demo.dto;

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
public class TripExpenseRequest {
    private Long tripId;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDateTime expenseDate;
    private String memo;
}
