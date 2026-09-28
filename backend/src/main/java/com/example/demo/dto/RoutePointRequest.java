package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import com.example.demo.domain.Itinerary;
import com.example.demo.domain.RoutePoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePointRequest {
    @NotBlank(message = "일정 이름은 필수입니다.")
    @Size(max = 200, message = "일정 이름은 200자 이내여야 합니다.")
    private String label;
    @Min(value = 1, message = "일차는 1 이상이어야 합니다.")
    private int dayNumber;
    private String dayLabel;
    @Min(value = 0, message = "순서는 0 이상이어야 합니다.")
    private int sequenceOrder;
    private String startTime;
    private String endTime;
    private Boolean isCompleted;
    private Double lat;
    private Double lng;
    @Size(max = 500, message = "메모는 500자 이내여야 합니다.")
    private String memo;

    public RoutePoint toEntity(Itinerary itinerary) {
        return RoutePoint.builder()
                .label(this.label)
                .dayNumber(this.dayNumber)
                .dayLabel(this.dayLabel)
                .sequenceOrder(this.sequenceOrder)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .isCompleted(this.isCompleted != null && this.isCompleted)
                .lat(this.lat)
                .lng(this.lng)
                .memo(this.memo)
                .itinerary(itinerary)
                .build();
    }
}
