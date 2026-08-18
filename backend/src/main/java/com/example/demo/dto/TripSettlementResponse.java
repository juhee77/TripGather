package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSettlementResponse {
    private Long tripId;
    private BigDecimal totalAmount;
    private int memberCount;
    private BigDecimal perPersonAmount;
    private List<PayerSummary> payerSummaries;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayerSummary {
        private Long userId;
        private String userName;
        private BigDecimal totalPaid;
        private BigDecimal balance; // +면 받아야 할 금액, -면 보내야 할 금액
    }
}
