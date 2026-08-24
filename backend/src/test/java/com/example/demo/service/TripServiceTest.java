package com.example.demo.service;

import com.example.demo.domain.Trip;
import com.example.demo.domain.User;
import com.example.demo.dto.TripRequest;
import com.example.demo.dto.TripResponse;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.TripRepository;
import com.example.demo.security.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private PackingService packingService;

    @Mock
    private ProfanityFilterService profanityFilterService;

    @InjectMocks
    private TripService tripService;

    @Test
    @DisplayName("여행 생성 성공")
    void createTrip_Success() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        TripRequest request = TripRequest.builder()
                .title("Jeju Summer Trip")
                .destination("Jeju")
                .country("Korea")
                .build();

        Trip savedTrip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        savedTrip.setId(10L);

        given(securityService.getCurrentUser()).willReturn(owner);
        given(tripRepository.save(any(Trip.class))).willReturn(savedTrip);

        // when
        TripResponse response = tripService.createTrip(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Jeju Summer Trip");
    }

    @Test
    @DisplayName("여행 생성 시 공백 제목 입력 시 예외 발생")
    void createTrip_EmptyTitle_ThrowsException() {
        // given
        TripRequest request = TripRequest.builder().title("   ").build();

        // when & then
        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행 제목을 입력해주세요.");
    }
}
