package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.ItineraryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItineraryServiceImplTest {

    @Mock
    private ItineraryRepository itineraryRepository;

    @InjectMocks
    private ItineraryServiceImpl itineraryService;

    @Test
    @DisplayName("상세 조회 실패 - 존재하지 않는 일정")
    void getById_Fail_NotFound() {
        // given
        Long id = 1L;
        given(itineraryRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> itineraryService.getById(id));
    }

    @Test
    @DisplayName("일정 생성 - RoutePoints가 null일 때")
    void createItinerary_NoRoutePoints() {
        // given
        Itinerary itinerary = Itinerary.builder().title("No Route").build();
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary saved = itineraryService.createItinerary(itinerary);

        // then
        assertThat(saved.getTitle()).isEqualTo("No Route");
        verify(itineraryRepository).save(itinerary);
    }

    @Test
    @DisplayName("일정 수정 - RoutePoints가 null일 때")
    void updateItinerary_NoRoutePoints() {
        // given
        Long id = 1L;
        Itinerary existing = Itinerary.builder().id(id).title("Old").routePoints(new ArrayList<>()).build();
        Itinerary update = Itinerary.builder().title("New").build();

        given(itineraryRepository.findById(id)).willReturn(Optional.of(existing));
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary updated = itineraryService.updateItinerary(id, update);

        // then
        assertThat(updated.getTitle()).isEqualTo("New");
        verify(itineraryRepository).save(existing);
    }
    @Test
    @DisplayName("일정 수정 - RoutePoints가 있을 때")
    void updateItinerary_WithRoutePoints() {
        // given
        Long id = 1L;
        Itinerary existing = Itinerary.builder().id(id).title("Old").routePoints(new ArrayList<>()).build();
        com.example.demo.domain.RoutePoint rp = com.example.demo.domain.RoutePoint.builder().label("Point").build();
        Itinerary update = Itinerary.builder().title("New").routePoints(java.util.List.of(rp)).build();

        given(itineraryRepository.findById(id)).willReturn(Optional.of(existing));
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary updated = itineraryService.updateItinerary(id, update);

        // then
        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getRoutePoints()).hasSize(1);
        verify(itineraryRepository).save(existing);
    }
}
