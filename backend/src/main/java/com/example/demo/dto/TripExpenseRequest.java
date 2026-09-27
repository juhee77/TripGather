package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
    @NotNull(message = "여행 ID는 필수입니다.")
    private Long tripId;
    @NotBlank(message = "지출 항목명은 필수입니다.")
    @Size(max = 100, message = "지출 항목명은 100자 이내여야 합니다.")
    private String title;
    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.0", message = "금액은 0 이상이어야 합니다.")
    @Digits(integer = 10, fraction = 2, message = "금액 형식이 올바르지 않습니다.")
    private BigDecimal amount;
    @Size(max = 50, message = "분류는 50자 이내여야 합니다.")
    private String category;
    private LocalDateTime expenseDate;
    @Size(max = 500, message = "메모는 500자 이내여야 합니다.")
    private String memo;
}
