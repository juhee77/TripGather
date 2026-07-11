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

    @Mock
    private com.example.demo.repository.UserRepository userRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private ItineraryServiceImpl itineraryService;

    @Test
    @DisplayName("일정 생성 - RoutePoints와 함께 생성")
    void createItinerary_WithRoutePoints() {
        // given
        Itinerary itinerary = Itinerary.builder().title("With Points").build();
        com.example.demo.domain.RoutePoint rp = com.example.demo.domain.RoutePoint.builder().label("P1").build();
        itinerary.setRoutePoints(new ArrayList<>(java.util.List.of(rp)));
        
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary saved = itineraryService.createItinerary(itinerary);

        // then
        assertThat(saved.getRoutePoints().get(0).getItinerary()).isEqualTo(saved);
        verify(itineraryRepository).save(itinerary);
    }

    @Test
    @DisplayName("일정 복제 성공")
    void cloneItinerary_Success() {
        // given
        Long originalId = 1L;
        com.example.demo.domain.RoutePoint rp = com.example.demo.domain.RoutePoint.builder().label("P1").build();
        Itinerary original = Itinerary.builder()
                .id(originalId)
                .title("Original")
                .routePoints(new ArrayList<>(java.util.List.of(rp)))
                .build();

        given(itineraryRepository.findById(originalId)).willReturn(Optional.of(original));
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary cloned = itineraryService.cloneItinerary(originalId, "newOwner@ex.com");

        // then
        assertThat(cloned.getOriginalId()).isEqualTo(originalId);
        assertThat(cloned.getOwnerEmail()).isEqualTo("newOwner@ex.com");
        assertThat(cloned.getTitle()).contains("(Copy)");
        assertThat(cloned.getRoutePoints()).hasSize(1);
    }

    @Test
    @DisplayName("공개 상태 전환 성공")
    void togglePublicStatus_Success() {
        // given
        Long id = 1L;
        String email = "owner@ex.com";
        Itinerary itinerary = Itinerary.builder().id(id).ownerEmail(email).publicStatus(false).build();

        given(itineraryRepository.findById(id)).willReturn(Optional.of(itinerary));
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary result = itineraryService.togglePublicStatus(id, email, true);

        // then
        assertThat(result.isPublicStatus()).isTrue();
    }

    @Test
    @DisplayName("공개 상태 전환 실패 - 소유자가 아님")
    void togglePublicStatus_Fail_NotOwner() {
        // given
        Long id = 1L;
        Itinerary itinerary = Itinerary.builder().id(id).ownerEmail("owner@ex.com").build();
        given(itineraryRepository.findById(id)).willReturn(Optional.of(itinerary));

        // when & then
        assertThrows(CustomException.class, () -> itineraryService.togglePublicStatus(id, "hacker@ex.com", true));
    }

    @Test
    @DisplayName("일정 병합 성공")
    void mergeItinerary_Success() {
        // given
        Long sId = 1L;
        Long tId = 2L;
        com.example.demo.domain.RoutePoint sPoint = com.example.demo.domain.RoutePoint.builder().label("Source P").build();
        Itinerary source = Itinerary.builder().id(sId).routePoints(new ArrayList<>(java.util.List.of(sPoint))).build();
        
        com.example.demo.domain.RoutePoint tPoint = com.example.demo.domain.RoutePoint.builder()
                .label("Target P")
                .dayNumber(1)
                .dayLabel("Day 1")
                .sequenceOrder(1)
                .build();
        Itinerary target = Itinerary.builder().id(tId).routePoints(new ArrayList<>(java.util.List.of(tPoint))).build();

        given(itineraryRepository.findById(sId)).willReturn(Optional.of(source));
        given(itineraryRepository.findById(tId)).willReturn(Optional.of(target));
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary result = itineraryService.mergeItinerary(sId, tId, 1);

        // then
        assertThat(result.getRoutePoints()).hasSize(2);
        assertThat(result.getRoutePoints().get(1).getLabel()).isEqualTo("Source P");
        assertThat(result.getRoutePoints().get(1).getSequenceOrder()).isEqualTo(2);
    }
}
