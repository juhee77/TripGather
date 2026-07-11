package com.example.demo.service;

import com.example.demo.domain.Trip;
import com.example.demo.domain.TripReview;
import com.example.demo.domain.User;
import com.example.demo.dto.TripReviewResponse;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.TripRepository;
import com.example.demo.repository.TripReviewRepository;
import com.example.demo.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripReviewService {

    private final TripReviewRepository tripReviewRepository;
    private final TripRepository tripRepository;
    private final SecurityService securityService;
    private final PointService pointService;

    @Transactional
    public TripReviewResponse createReview(Long tripId, String content, int rating, String category, String imageUrls) {
        User author = securityService.getCurrentUser();
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다."));
        TripReview review = TripReview.of(trip, author, content, rating, category);
        review.setImageUrls(imageUrls);
        
        TripReviewResponse response = TripReviewResponse.from(tripReviewRepository.save(review));
        
        // 후기 작성 성공 시 100포인트 적립
        pointService.addPoints(author.getId(), 100, 0, "여행 후기 작성");
        
        return response;
    }

    @Transactional(readOnly = true)
    public List<TripReviewResponse> getReviews(Long tripId, String category) {
        if (category != null && !category.isBlank()) {
            return tripReviewRepository.findByTripIdAndCategoryOrderByCreatedAtDesc(tripId, category)
                    .stream().map(TripReviewResponse::from).toList();
        }
        return tripReviewRepository.findByTripIdOrderByCreatedAtDesc(tripId)
                .stream().map(TripReviewResponse::from).toList();
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        TripReview review = tripReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "리뷰를 찾을 수 없습니다."));
        String email = securityService.getCurrentUserEmail();
        if (!review.getAuthor().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "본인의 리뷰만 삭제할 수 있습니다.");
        }
        tripReviewRepository.delete(review);
    }
}
