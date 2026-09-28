package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {
    @NotBlank(message = "여행 제목은 필수입니다.")
    @Size(max = 100, message = "여행 제목은 100자 이내여야 합니다.")
    private String title;
    @Size(max = 100, message = "여행지는 100자 이내여야 합니다.")
    private String destination;
    @Size(max = 100, message = "국가는 100자 이내여야 합니다.")
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    /** base64 data URI 를 그대로 담을 수 있어 길이를 제한하지 않는다 (컬럼 타입 TEXT). */
    private String bgImageUrl;
    private String status;
    private Long itineraryId;
}
