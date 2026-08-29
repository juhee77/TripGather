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

    @Test
    @DisplayName("여행 생성 시 종료일이 시작일보다 빠를 경우 예외 발생")
    void createTrip_InvalidDateRange_ThrowsException() {
        // given
        TripRequest request = TripRequest.builder()
                .title("Jeju Trip")
                .startDate(java.time.LocalDate.of(2026, 8, 30))
                .endDate(java.time.LocalDate.of(2026, 8, 20))
                .build();

        // when & then
        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("종료일은 시작일보다 빠를 수 없습니다.");
    }

    @Test
    @DisplayName("여행 생성 시 비속어 제목 입력 시 예외 발생")
    void createTrip_ProfanityTitle_ThrowsException() {
        // given
        TripRequest request = TripRequest.builder().title("개새끼 여행").build();
        org.mockito.BDDMockito.willThrow(new CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText("개새끼 여행");

        // when & then
        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(CustomException.class);
    }
}
