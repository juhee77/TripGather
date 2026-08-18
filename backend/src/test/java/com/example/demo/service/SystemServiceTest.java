package com.example.demo.service;

import com.example.demo.dto.SystemStatsResponse;
import com.example.demo.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SystemServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private StampRepository stampRepository;

    @Mock
    private TripExpenseRepository tripExpenseRepository;

    @InjectMocks
    private SystemService systemService;

    @Test
    @DisplayName("시스템 요약 통계 계산 성공")
    void getSystemStats_Success() {
        // given
        given(userRepository.count()).willReturn(10L);
        given(gatheringRepository.count()).willReturn(5L);
        given(itineraryRepository.count()).willReturn(8L);
        given(stampRepository.count()).willReturn(12L);
        given(tripExpenseRepository.count()).willReturn(15L);

        // when
        SystemStatsResponse stats = systemService.getSystemStats();

        // then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalUsers()).isEqualTo(10L);
        assertThat(stats.getTotalGatherings()).isEqualTo(5L);
        assertThat(stats.getTotalItineraries()).isEqualTo(8L);
        assertThat(stats.getTotalStamps()).isEqualTo(12L);
        assertThat(stats.getTotalExpenses()).isEqualTo(15L);
    }
}
