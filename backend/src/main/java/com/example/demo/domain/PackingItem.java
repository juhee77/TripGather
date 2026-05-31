package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packing_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private String category = "기타";

    @Builder.Default
    private boolean checked = false;

    public static PackingItem of(Trip trip, String name, String category) {
        return PackingItem.builder()
                .trip(trip)
                .name(name)
                .category(category)
                .build();
    }
}
