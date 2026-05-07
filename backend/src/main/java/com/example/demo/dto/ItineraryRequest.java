package com.example.demo.dto;

import com.example.demo.domain.Itinerary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryRequest {
    private String title;
    private String description;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String author;
    private String authorEmail;
    private String ownerEmail;
    private boolean publicStatus;
    private String stampImageUrl;
    private List<RoutePointRequest> routePoints;

    public Itinerary toEntity() {
        Itinerary itinerary = Itinerary.builder()
                .title(this.title)
                .description(this.description)
                .location(this.location)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .author(this.author)
                .authorEmail(this.authorEmail)
                .ownerEmail(this.ownerEmail)
                .publicStatus(this.publicStatus)
                .stampImageUrl(this.stampImageUrl)
                .build();
        
        if (this.routePoints != null) {
            itinerary.setRoutePoints(this.routePoints.stream()
                    .map(rp -> rp.toEntity(itinerary))
                    .collect(Collectors.toList()));
        }
        
        return itinerary;
    }
}
