package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatsResponse {
    private long totalUsers;
    private long totalGatherings;
    private long totalItineraries;
    private long totalStamps;
    private long totalExpenses;
}
