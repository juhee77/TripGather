package com.example.demo.dto;

import com.example.demo.domain.Gathering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringRequest {
    private String title;
    private String location;
    private Double lat;
    private Double lng;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxJoining;
    private String bgImageUrl;
    private boolean isGalleryPublic;
    private boolean isChatPublic;
    private boolean isCommentPublic;
    private Long linkedItineraryId;

    public Gathering toEntity() {
        Gathering gathering = Gathering.builder()
                .title(this.title)
                .location(this.location)
                .lat(this.lat)
                .lng(this.lng)
                .category(this.category)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .maxJoining(this.maxJoining)
                .bgImageUrl(this.bgImageUrl)
                .isGalleryPublic(this.isGalleryPublic)
                .isChatPublic(this.isChatPublic)
                .isCommentPublic(this.isCommentPublic)
                .build();
        
        if (this.linkedItineraryId != null) {
            gathering.setLinkedItinerary(com.example.demo.domain.Itinerary.builder()
                    .id(this.linkedItineraryId)
                    .build());
        }
        
        return gathering;
    }
}
