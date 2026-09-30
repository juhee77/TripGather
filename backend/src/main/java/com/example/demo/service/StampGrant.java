package com.example.demo.service;

import java.time.LocalDate;

/**
 * 포인트 적립과 함께 발급할 스탬프의 정보.
 *
 * 모임 스탬프와 코스 스탬프는 가리키는 대상이 다르고, 모임 스탬프에는 회차까지 따라붙는다.
 * 이것들을 addPoints 의 인자로 나열하면 의미를 알 수 없는 null 이 줄지어 서기 때문에 묶어서 넘긴다.
 */
public record StampGrant(
        Long gatheringId,
        Long itineraryId,
        LocalDate occurrenceDate,
        String imageUrl
) {

    /** 모임 참여 스탬프. occurrenceDate 는 몇 회차에 나왔는지를 남긴다. */
    public static StampGrant forGathering(Long gatheringId, LocalDate occurrenceDate, String imageUrl) {
        return new StampGrant(gatheringId, null, occurrenceDate, imageUrl);
    }

    /** 코스 완주 스탬프. 회차 개념이 없다. */
    public static StampGrant forItinerary(Long itineraryId, String imageUrl) {
        return new StampGrant(null, itineraryId, null, imageUrl);
    }
}
