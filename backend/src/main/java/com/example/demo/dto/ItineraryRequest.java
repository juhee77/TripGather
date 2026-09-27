package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "코스 제목은 필수입니다.")
    @Size(max = 100, message = "코스 제목은 100자 이내여야 합니다.")
    private String title;
    @Size(max = 2000, message = "코스 설명은 2000자 이내여야 합니다.")
    private String description;
    @Size(max = 200, message = "장소는 200자 이내여야 합니다.")
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String author;
    private String authorEmail;
    private String ownerEmail;
    private boolean publicStatus;
    private String stampImageUrl;
    @Valid
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
