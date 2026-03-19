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
    private LocalDateTime createdAt;
    // Map RoutePoints logic can be added if needed on frontend, 
    // but for now let's focus on the fields from Itinerary entity.

    public static ItineraryResponse from(Itinerary itinerary) {
        if (itinerary == null) return null;
        return ItineraryResponse.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .author(itinerary.getAuthor())
                .description(itinerary.getDescription())
                .createdAt(itinerary.getCreatedAt())
                .build();
    }
}
