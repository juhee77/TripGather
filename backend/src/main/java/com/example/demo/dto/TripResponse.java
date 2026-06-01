package com.example.demo.dto;

import com.example.demo.domain.Trip;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {
    private Long id;
    private String title;
    private String destination;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private String bgImageUrl;
    private String status;
    private UserResponse owner;
    private int itineraryCount;
    private int packingProgress;
    private int reviewCount;

    public static TripResponse from(Trip trip) {
        if (trip == null) return null;
        int totalPacking = trip.getPackingItems() != null ? trip.getPackingItems().size() : 0;
        int checkedPacking = trip.getPackingItems() != null ?
                (int) trip.getPackingItems().stream().filter(p -> p.isChecked()).count() : 0;

        // 여행 생애주기(Lifecycle) 동적 계산
        String calculatedStatus = trip.getStatus().name();
        LocalDate today = LocalDate.now();
        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            if (today.isBefore(trip.getStartDate())) {
                calculatedStatus = "PLANNING";
            } else if (today.isAfter(trip.getEndDate())) {
                calculatedStatus = "COMPLETED";
            } else {
                calculatedStatus = "ONGOING";
            }
        }

        return TripResponse.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .destination(trip.getDestination())
                .country(trip.getCountry())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .bgImageUrl(trip.getBgImageUrl())
                .status(calculatedStatus)
                .owner(UserResponse.from(trip.getOwner()))
                .itineraryCount(trip.getTripItineraries() != null ? trip.getTripItineraries().size() : 0)
                .packingProgress(totalPacking > 0 ? (checkedPacking * 100 / totalPacking) : 0)
                .reviewCount(trip.getReviews() != null ? trip.getReviews().size() : 0)
                .build();
    }
}
