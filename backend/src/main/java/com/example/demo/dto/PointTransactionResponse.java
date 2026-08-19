package com.example.demo.dto;

import com.example.demo.domain.PointTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransactionResponse {
    private Long id;
    private int amount;
    private String transactionType;
    private String description;
    private LocalDateTime createdAt;

    public static PointTransactionResponse from(PointTransaction tx) {
        return PointTransactionResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .transactionType(tx.getTransactionType())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
