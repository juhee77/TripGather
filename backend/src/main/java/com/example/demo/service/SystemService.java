package com.example.demo.service;

import com.example.demo.dto.SystemStatsResponse;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemService {

    private final UserRepository userRepository;
    private final GatheringRepository gatheringRepository;
    private final ItineraryRepository itineraryRepository;
    private final StampRepository stampRepository;
    private final TripExpenseRepository tripExpenseRepository;

    public SystemStatsResponse getSystemStats() {
        return SystemStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalGatherings(gatheringRepository.count())
                .totalItineraries(itineraryRepository.count())
                .totalStamps(stampRepository.count())
                .totalExpenses(tripExpenseRepository.count())
                .build();
    }
}
