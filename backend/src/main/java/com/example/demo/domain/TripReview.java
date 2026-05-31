package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private int rating = 5;

    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    @Builder.Default
    private String category = "관광지";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static TripReview of(Trip trip, User author, String content, int rating, String category) {
        return TripReview.builder()
                .trip(trip)
                .author(author)
                .content(content)
                .rating(rating)
                .category(category)
                .build();
    }
}
