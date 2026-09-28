package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
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
    @NotBlank(message = "모임 제목은 필수입니다.")
    @Size(max = 100, message = "모임 제목은 100자 이내여야 합니다.")
    private String title;
    @Size(max = 200, message = "장소는 200자 이내여야 합니다.")
    private String location;
    private Double lat;
    private Double lng;
    @Size(max = 50, message = "카테고리는 50자 이내여야 합니다.")
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    @Min(value = 1, message = "정원은 1명 이상이어야 합니다.")
    @Max(value = 1000, message = "정원은 1000명을 넘을 수 없습니다.")
    private int maxJoining;
    /** base64 data URI 를 그대로 담을 수 있어 길이를 제한하지 않는다 (컬럼 타입 TEXT). */
    private String bgImageUrl;
    private boolean isGalleryPublic;
    private boolean isChatPublic;
    private boolean isCommentPublic;
    private Long linkedItineraryId;

    /** 반복 규칙. 생략하면 일회성(NONE). */
    private com.example.demo.domain.RecurrenceRule recurrenceRule;
    /** WEEKLY 일 때 반복 요일 (MONDAY ~ SUNDAY) */
    private java.time.DayOfWeek recurrenceDayOfWeek;
    /** 반복 종료일(포함). 생략하면 기한 없음. */
    private LocalDate recurrenceUntil;

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
                .recurrenceRule(this.recurrenceRule != null
                        ? this.recurrenceRule : com.example.demo.domain.RecurrenceRule.NONE)
                .recurrenceDayOfWeek(this.recurrenceDayOfWeek)
                .recurrenceUntil(this.recurrenceUntil)
                .build();
        
        if (this.linkedItineraryId != null) {
            gathering.setLinkedItinerary(com.example.demo.domain.Itinerary.builder()
                    .id(this.linkedItineraryId)
                    .build());
        }
        
        return gathering;
    }
}
