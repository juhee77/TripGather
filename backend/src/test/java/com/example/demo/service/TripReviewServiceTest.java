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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripReviewServiceTest {

    @Mock
    private TripReviewRepository tripReviewRepository;
    @Mock
    private com.example.demo.repository.TripRepository tripRepository;
    @Mock
    private com.example.demo.security.SecurityService securityService;
    @Mock
    private PointService pointService;
    @Mock
    private ProfanityFilterService profanityFilterService;

    @InjectMocks
    private TripReviewService tripReviewService;

    @Test
    @DisplayName("여행 후기 요약 통계 계산 - 총 2개 후기 평균 평점 4.5")
    void getReviewSummary_Success() {
        // given
        Long tripId = 1L;
        given(tripRepository.existsById(tripId)).willReturn(true);
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

    @Test
    @DisplayName("비속어 포함 여행 후기 작성 시 예외 발생")
    void createReview_ProfanityContent_ThrowsException() {
        // given
        String profanityContent = "진짜 개새끼 최악의 숙소";
        org.mockito.BDDMockito.willThrow(new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText(profanityContent);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.createReview(1L, profanityContent, 1, "숙소", null))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("유효하지 않은 평점(0점 또는 6점)으로 여행 후기 작성 시 예외 발생")
    void createReview_InvalidRating_ThrowsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.createReview(1L, "좋은 여행", 6, "관광지", null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("평점은 1점부터 5점 사이여야 합니다.");
    }

    @Test
    @DisplayName("카테고리 '전체' 지정 시 전체 리뷰 목록 반환")
    void getReviews_CategoryAll_ReturnsAllReviews() {
        // given
        Long tripId = 1L;
        given(tripRepository.existsById(tripId)).willReturn(true);
        given(tripReviewRepository.findByTripIdOrderByCreatedAtDesc(tripId)).willReturn(java.util.List.of());

        // when
        java.util.List<com.example.demo.dto.TripReviewResponse> reviews = tripReviewService.getReviews(tripId, "전체");

        // then
        assertThat(reviews).isEmpty();
        verify(tripReviewRepository).findByTripIdOrderByCreatedAtDesc(tripId);
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 리뷰 조회 시 예외 발생")
    void getReviews_TripNotFound_ThrowsException() {
        // given
        Long tripId = 99L;
        given(tripRepository.existsById(tripId)).willReturn(false);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripReviewService.getReviews(tripId, "전체"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공백 내용으로 여행 후기 작성 시 예외 발생")
    void createReview_EmptyContent_ThrowsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.createReview(1L, "   ", 5, "숙소", null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("리뷰 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("공백 내용으로 여행 후기 수정 시 예외 발생")
    void updateReview_EmptyContent_ThrowsException() {
        // given
        Long reviewId = 10L;
        User author = User.builder().id(1L).email("author@test.com").build();
        TripReview review = TripReview.builder().id(reviewId).author(author).build();
        given(tripReviewRepository.findById(reviewId)).willReturn(java.util.Optional.of(review));
        given(securityService.getCurrentUserEmail()).willReturn("author@test.com");

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.updateReview(reviewId, "   ", 5, "숙소", null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("리뷰 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 리뷰 요약 조회 시 예외 발생")
    void getReviewSummary_TripNotFound_ThrowsException() {
        // given
        Long tripId = 99L;
        given(tripRepository.existsById(tripId)).willReturn(false);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> tripReviewService.getReviewSummary(tripId))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("리뷰 작성 시 사진 5장 초과 첨부 시 예외 발생")
    void createReview_ExceedMaxImages_ThrowsException() {
        String sixImages = "img1.png,img2.png,img3.png,img4.png,img5.png,img6.png";
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.createReview(1L, "좋은 여행 후기", 5, "숙소", sixImages))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("리뷰 사진은 최대 5장까지 첨부할 수 있습니다.");
    }

    @Test
    @DisplayName("리뷰 수정 시 사진 5장 초과 첨부 시 예외 발생")
    void updateReview_ExceedMaxImages_ThrowsException() {
        // given
        Long reviewId = 10L;
        User author = User.builder().id(1L).email("author@test.com").build();
        TripReview review = TripReview.builder().id(reviewId).author(author).build();
        given(tripReviewRepository.findById(reviewId)).willReturn(java.util.Optional.of(review));
        given(securityService.getCurrentUserEmail()).willReturn("author@test.com");

        String sixImages = "img1.png,img2.png,img3.png,img4.png,img5.png,img6.png";

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.updateReview(reviewId, "수정된 여행 후기", 4, "숙소", sixImages))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("리뷰 사진은 최대 5장까지 첨부할 수 있습니다.");
    }

    @Test
    @DisplayName("리뷰 수정 시 유효하지 않은 평점(0점 또는 6점) 지정 시 예외 발생")
    void updateReview_InvalidRating_ThrowsException() {
        // given
        Long reviewId = 10L;
        User author = User.builder().id(1L).email("author@test.com").build();
        TripReview review = TripReview.builder().id(reviewId).author(author).build();
        given(tripReviewRepository.findById(reviewId)).willReturn(java.util.Optional.of(review));
        given(securityService.getCurrentUserEmail()).willReturn("author@test.com");

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.updateReview(reviewId, "수정된 여행 후기", 6, "숙소", null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("평점은 1점부터 5점 사이여야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 삭제 요청 시 예외 발생")
    void deleteReview_NotFound_ThrowsException() {
        // given
        Long reviewId = 99L;
        given(tripReviewRepository.findById(reviewId)).willReturn(java.util.Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                tripReviewService.deleteReview(reviewId))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("리뷰를 찾을 수 없습니다.");
    }
}
