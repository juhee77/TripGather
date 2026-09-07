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
import static org.mockito.Mockito.verify;

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

    @Test
    @DisplayName("기존 일정 ID를 지정해 여행을 만들면 해당 일정이 여행에 바인딩된다")
    void createTrip_WithExistingItinerary_BindsItinerary() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        TripRequest request = TripRequest.builder()
                .title("Jeju Trip")
                .destination("Jeju")
                .itineraryId(5L)
                .startDate(java.time.LocalDate.of(2026, 5, 1))
                .endDate(java.time.LocalDate.of(2026, 5, 3))
                .build();
        com.example.demo.domain.Itinerary existing = com.example.demo.domain.Itinerary.builder().id(5L).build();
        Trip savedTrip = Trip.of("Jeju Trip", "Jeju", null, owner);
        savedTrip.setId(10L);

        given(securityService.getCurrentUser()).willReturn(owner);
        given(itineraryRepository.findById(5L)).willReturn(java.util.Optional.of(existing));
        given(tripRepository.save(any(Trip.class))).willReturn(savedTrip);

        // when
        TripResponse response = tripService.createTrip(request);

        // then
        assertThat(response).isNotNull();
        assertThat(existing.getOwnerEmail()).isEqualTo("owner@test.com");
        assertThat(existing.getLocation()).isEqualTo("Jeju");
        verify(packingService).initDefaultItems(10L);
    }

    @Test
    @DisplayName("내 여행 목록 조회 성공")
    void getMyTrips_Success() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Busan Trip", "Busan", "Korea", owner);
        trip.setId(1L);
        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");
        given(tripRepository.findByOwnerEmailOrderByCreatedAtDesc("owner@test.com"))
                .willReturn(java.util.List.of(trip));

        // when
        var result = tripService.getMyTrips();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Busan Trip");
    }

    @Test
    @DisplayName("여행 단건 조회 성공")
    void getTrip_Success() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Busan Trip", "Busan", "Korea", owner);
        trip.setId(1L);
        given(tripRepository.findById(1L)).willReturn(java.util.Optional.of(trip));

        // when
        TripResponse response = tripService.getTrip(1L);

        // then
        assertThat(response.getTitle()).isEqualTo("Busan Trip");
    }

    @Test
    @DisplayName("여행 수정 성공 시 연결된 일정표 정보도 함께 갱신된다")
    void updateTrip_Success_SyncsItinerary() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Old Title", "Seoul", "Korea", owner);
        trip.setId(1L);
        com.example.demo.domain.Itinerary itinerary = com.example.demo.domain.Itinerary.builder().id(5L).build();
        trip.setItinerary(itinerary);

        TripRequest request = TripRequest.builder()
                .title("New Title")
                .destination("Busan")
                .country("Korea")
                .startDate(java.time.LocalDate.of(2026, 5, 1))
                .endDate(java.time.LocalDate.of(2026, 5, 5))
                .bgImageUrl("https://cdn/bg.png")
                .build();

        given(tripRepository.findById(1L)).willReturn(java.util.Optional.of(trip));
        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");
        given(tripRepository.save(any(Trip.class))).willAnswer(i -> i.getArgument(0));

        // when
        TripResponse response = tripService.updateTrip(1L, request);

        // then
        assertThat(response.getTitle()).isEqualTo("New Title");
        assertThat(itinerary.getTitle()).isEqualTo("New Title 일정표");
        assertThat(itinerary.getLocation()).isEqualTo("Busan");
        assertThat(itinerary.getStartDate()).isEqualTo(java.time.LocalDate.of(2026, 5, 1));
        assertThat(itinerary.getEndDate()).isEqualTo(java.time.LocalDate.of(2026, 5, 5));
    }

    @Test
    @DisplayName("타인의 여행 수정 시도 시 권한 예외 발생")
    void updateTrip_NotOwner_ThrowsForbidden() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").build();
        Trip trip = Trip.of("Trip", "Seoul", "Korea", owner);
        trip.setId(1L);
        given(tripRepository.findById(1L)).willReturn(java.util.Optional.of(trip));
        given(securityService.getCurrentUserEmail()).willReturn("hacker@test.com");

        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(1L, TripRequest.builder().title("New").build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인의 여행만 관리할 수 있습니다.");
    }

    @Test
    @DisplayName("본인 여행 삭제 성공")
    void deleteTrip_Success() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").build();
        Trip trip = Trip.of("Trip", "Seoul", "Korea", owner);
        trip.setId(1L);
        given(tripRepository.findById(1L)).willReturn(java.util.Optional.of(trip));
        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");

        // when
        tripService.deleteTrip(1L);

        // then
        verify(tripRepository).delete(trip);
    }

    @Test
    @DisplayName("목적지가 일치하는 공개 여정만 추천 목록에 포함된다")
    void getRecommendedItineraries_FiltersByDestinationAndVisibility() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").build();
        Trip trip = Trip.of("Busan Trip", "부산", "Korea", owner);
        trip.setId(1L);
        given(tripRepository.findById(1L)).willReturn(java.util.Optional.of(trip));
        given(itineraryRepository.findAll()).willReturn(java.util.List.of(
                com.example.demo.domain.Itinerary.builder().id(1L).title("부산 2박3일").location("부산 해운대구").publicStatus(true).build(),
                com.example.demo.domain.Itinerary.builder().id(2L).title("비공개 부산").location("부산").publicStatus(false).build(),
                com.example.demo.domain.Itinerary.builder().id(3L).title("제주 여행").location("제주도").publicStatus(true).build()
        ));

        // when
        var result = tripService.getRecommendedItineraries(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("부산 2박3일");
    }

    @Test
    @DisplayName("존재하지 않는 여행 조회 시 예외 발생")
    void getTrip_NotFound_ThrowsException() {
        // given
        given(tripRepository.findById(999L)).willReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.getTrip(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }
}
