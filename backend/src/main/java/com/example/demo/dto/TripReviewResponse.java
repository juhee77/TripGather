package com.example.demo.dto;

import com.example.demo.domain.TripReview;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripReviewResponse {
    private Long id;
    private String content;
    private int rating;
    private String imageUrls;
    private String category;
    private UserResponse author;
    private LocalDateTime createdAt;

    public static TripReviewResponse from(TripReview review) {
        return TripReviewResponse.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .imageUrls(review.getImageUrls())
                .category(review.getCategory())
                .author(UserResponse.from(review.getAuthor()))
                .createdAt(review.getCreatedAt())
                .build();
    }
}
