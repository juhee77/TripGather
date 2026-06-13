package com.example.demo.dto;

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
    private String label;
    private int dayNumber;
    private String dayLabel;
    private int sequenceOrder;
    private String startTime;
    private String endTime;
    private Boolean isCompleted;
    private Double lat;
    private Double lng;

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
                .itinerary(itinerary)
                .build();
    }
}
