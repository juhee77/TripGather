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

    @Test
    @DisplayName("여행 생성 시 존재하지 않는 일정 ID 지정 시 예외 발생")
    void createTrip_NonExistingItinerary_ThrowsException() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        TripRequest request = TripRequest.builder()
                .title("Jeju Summer Trip")
                .itineraryId(999L)
                .build();

        given(securityService.getCurrentUser()).willReturn(owner);
        given(itineraryRepository.findById(999L)).willReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.createTrip(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("지정된 일정을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("여행 정보 수정 시 공백 제목 전달 시 예외 발생")
    void updateTrip_EmptyTitle_ThrowsException() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);

        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");
        given(tripRepository.findById(tripId)).willReturn(java.util.Optional.of(trip));

        TripRequest request = TripRequest.builder().title("   ").build();

        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(tripId, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행 제목은 공백일 수 없습니다.");
    }

    @Test
    @DisplayName("여행 정보 수정 시 종료일이 시작일보다 빠른 날짜 전달 시 예외 발생")
    void updateTrip_InvalidDateRange_ThrowsException() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);
        trip.setStartDate(java.time.LocalDate.of(2026, 8, 30));
        trip.setEndDate(java.time.LocalDate.of(2026, 9, 5));

        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");
        given(tripRepository.findById(tripId)).willReturn(java.util.Optional.of(trip));

        TripRequest request = TripRequest.builder()
                .endDate(java.time.LocalDate.of(2026, 8, 20))
                .build();

        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(tripId, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("종료일은 시작일보다 빠를 수 없습니다.");
    }

    @Test
    @DisplayName("여행 단건 조회 시 null tripId 전달 시 예외 발생")
    void getTrip_NullTripId_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> tripService.getTrip(null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("여행 목적지가 설정되지 않은 경우 추천 여정 목록 조회 시 빈 리스트 반환")
    void getRecommendedItineraries_BlankDestination_ReturnsEmptyList() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "", "Korea", owner);
        trip.setId(tripId);

        given(tripRepository.findById(tripId)).willReturn(java.util.Optional.of(trip));

        // when
        java.util.List<com.example.demo.dto.ItineraryResponse> responses = tripService.getRecommendedItineraries(tripId);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("여행 목록 조회 시 공백 이메일 전달 시 예외 발생")
    void getMyTrips_EmptyEmail_ThrowsException() {
        // given
        given(securityService.getCurrentUserEmail()).willReturn("   ");

        // when & then
        assertThatThrownBy(() -> tripService.getMyTrips())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("유저 이메일 정보가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("여행 삭제 시 null tripId 전달 시 예외 발생")
    void deleteTrip_NullTripId_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> tripService.deleteTrip(null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행 ID가 올바르지 않습니다.");
    }
}
