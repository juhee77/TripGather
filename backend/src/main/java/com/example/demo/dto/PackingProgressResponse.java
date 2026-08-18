package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingProgressResponse {
    private Long tripId;
    private int totalCount;
    private int checkedCount;
    private double progressPercentage;
}
