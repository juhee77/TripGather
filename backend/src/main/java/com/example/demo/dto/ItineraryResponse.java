package com.example.demo.dto;

import com.example.demo.domain.Itinerary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryResponse {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String location;
    private String dates;
    private String bgImageUrl;
    private String stampImageUrl;
    private LocalDateTime createdAt;
    private List<RoutePointResponse> routePoints;

    public static ItineraryResponse from(Itinerary itinerary) {
        if (itinerary == null) return null;
        return ItineraryResponse.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .author(itinerary.getAuthor())
                .description(itinerary.getDescription())
                .location(itinerary.getLocation())
                .dates(itinerary.getDates())
                .bgImageUrl(itinerary.getBgImageUrl())
                .stampImageUrl(itinerary.getStampImageUrl())
                .createdAt(itinerary.getCreatedAt())
                .routePoints(itinerary.getRoutePoints() != null ? 
                        itinerary.getRoutePoints().stream()
                                .map(RoutePointResponse::from)
                                .toList() : null)
                .build();
    }
}
