package com.example.demo.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {
    private String title;
    private String destination;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bgImageUrl;
    private String status;
}
