package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Itinerary itinerary;

    /** DAY 번호 (1, 2, 3...). 날짜별 그룹핑에 사용 */
    @Builder.Default
    private int dayNumber = 1;

    /** DAY 라벨 (예: "Day 1", "2026-08-01", "첫째날") */
    private String dayLabel;

    /** DAY 내 순서 */
    private int sequenceOrder;

    private String label;

    private Double lat;

    private Double lng;
}
