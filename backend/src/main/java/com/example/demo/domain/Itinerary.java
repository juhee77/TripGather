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
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Itinerary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;
    
    private String authorEmail;

    /**
     * ownerEmail represents the user who currently "owns" this instance of the itinerary.
     * When cloned, this becomes the user who added it to their trips.
     */
    private String ownerEmail;

    /**
     * originalId tracks the source itinerary if this is a clone.
     */
    private Long originalId;

    /**
     * isPublic determines if this itinerary shows up in the "Travel Feed".
     */
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("isPublic")
    private boolean publicStatus = false;

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


}
