package com.example.demo.dto;

import com.example.demo.domain.RoutePoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePointResponse {
    private Long id;
    private int dayNumber;
    private String dayLabel;
    private int sequenceOrder;
    private String label;
    private String visitTime;
    private Double lat;
    private Double lng;

    public static RoutePointResponse from(RoutePoint routePoint) {
        if (routePoint == null) return null;
        return RoutePointResponse.builder()
                .id(routePoint.getId())
                .dayNumber(routePoint.getDayNumber())
                .dayLabel(routePoint.getDayLabel())
                .sequenceOrder(routePoint.getSequenceOrder())
                .label(routePoint.getLabel())
                .visitTime(routePoint.getVisitTime())
                .lat(routePoint.getLat())
                .lng(routePoint.getLng())
                .build();
    }
}
