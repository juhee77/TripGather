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
    private String authorEmail;
    private String ownerEmail;
    private Long originalId;
    private boolean isPublic;
    private String description;
    private String location;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
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
                .authorEmail(itinerary.getAuthorEmail())
                .ownerEmail(itinerary.getOwnerEmail())
                .originalId(itinerary.getOriginalId())
                .isPublic(itinerary.isPublic())
                .description(itinerary.getDescription())
                .location(itinerary.getLocation())
                .startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate())
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
