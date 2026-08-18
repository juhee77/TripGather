package com.example.demo.service;

import com.example.demo.domain.Trip;
import com.example.demo.domain.TripReview;
import com.example.demo.domain.User;
import com.example.demo.dto.TripReviewSummaryResponse;
import com.example.demo.repository.TripReviewRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TripReviewServiceTest {

    @Mock
    private TripReviewRepository tripReviewRepository;

    @InjectMocks
    private TripReviewService tripReviewService;

    @Test
    @DisplayName("여행 후기 요약 통계 계산 - 총 2개 후기 평균 평점 4.5")
    void getReviewSummary_Success() {
        // given
        Long tripId = 1L;
        Trip trip = Trip.builder().id(tripId).title("Busan").build();
        User author = User.builder().id(10L).name("Reviewer").build();

        TripReview r1 = TripReview.of(trip, author, "Great spot!", 5, "관광지");
        TripReview r2 = TripReview.of(trip, author, "Nice hotel", 4, "숙소");

        given(tripReviewRepository.findByTripIdOrderByCreatedAtDesc(tripId)).willReturn(List.of(r1, r2));

        // when
        TripReviewSummaryResponse summary = tripReviewService.getReviewSummary(tripId);

        // then
        assertThat(summary).isNotNull();
        assertThat(summary.getTotalReviews()).isEqualTo(2);
        assertThat(summary.getAverageRating()).isEqualTo(4.5);
    }
}
