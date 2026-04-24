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
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;
    
    private String authorEmail;

    private String description;

    private String location;

    private java.time.LocalDate startDate;

    private java.time.LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String bgImageUrl;

    @Column(columnDefinition = "TEXT")
    private String stampImageUrl;

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<RoutePoint> routePoints = new java.util.ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
