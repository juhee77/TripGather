package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 여행 리뷰 작성·수정 본문.
 *
 * 예전에는 Map&lt;String, Object&gt; 를 그대로 받아 필드마다 형변환을 했다.
 * 그래서 rating 을 "5" 처럼 문자열로 보내면 ClassCastException 이 나고
 * 처리기를 거쳐 500 으로 떨어졌다. 타입을 명시해 잘못된 형식은 역직렬화 단계에서 400 이 되게 한다.
 *
 * 내용·평점·사진 장수 규칙은 TripReviewService 가 이미 검증한다.
 * 규칙의 주인을 한 곳으로 두기 위해 여기서는 같은 제약을 다시 걸지 않는다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripReviewRequest {

    private String content;

    /** 생략하면 5점. */
    @Builder.Default
    private Integer rating = 5;

    /** 생략하면 "관광지". */
    @Builder.Default
    private String category = "관광지";

    /** 쉼표로 구분한 사진 주소 목록. */
    private String imageUrls;

    public int ratingOrDefault() {
        return rating != null ? rating : 5;
    }

    public String categoryOrDefault() {
        return category != null ? category : "관광지";
    }
}
