package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    private String status; // "ACTIVE" or "COMPLETED"
    private String stampImageUrl;

    @OneToMany(mappedBy = "userMission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<UserMissionStep> steps = new java.util.ArrayList<>();

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
