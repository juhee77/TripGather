package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_itinerary",
       uniqueConstraints = @UniqueConstraint(columnNames = {"trip_id", "itinerary_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripItinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @Builder.Default
    private int displayOrder = 0;

    public static TripItinerary of(Trip trip, Itinerary itinerary, int displayOrder) {
        return TripItinerary.builder()
                .trip(trip)
                .itinerary(itinerary)
                .displayOrder(displayOrder)
                .build();
    }
}
