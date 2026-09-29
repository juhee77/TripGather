package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
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
    /**
     * 길이 제한은 GatheringServiceImpl 이 판단한다(비속어 검사와 같은 자리에서 이뤄진다).
     * 비어 있는지만 여기서 본다 — 컬럼이 NOT NULL 이라 null 이 그대로 내려가면 500 이 된다.
     */
    @NotBlank(message = "모임 제목은 필수입니다.")
    private String title;
    @NotBlank(message = "만나는 장소는 필수입니다.")
    private String location;
    private Double lat;
    private Double lng;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    /**
     * 정원의 허용 범위(2~100명)는 GatheringServiceImpl 이 판단한다.
     * 현재 참여 인원보다 적게 줄일 수 없다는 규칙이 함께 걸려 있어
     * 요청 본문만 보고는 결론을 낼 수 없기 때문이다.
     */
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
