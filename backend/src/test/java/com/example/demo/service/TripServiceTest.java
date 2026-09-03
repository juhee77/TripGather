package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.domain.Trip;
import com.example.demo.domain.TripStatus;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("여행 생성 시 기존 일정 ID와 상태를 지정하면 해당 일정에 여행 정보 바인딩")
    void createTrip_WithExistingItineraryAndStatus_BindsItinerary() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Itinerary existing = Itinerary.builder().id(5L).title("복제된 일정").build();
        TripRequest request = TripRequest.builder()
                .title("Jeju Summer Trip")
                .destination("Jeju")
                .country("Korea")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .status("ONGOING")
                .itineraryId(5L)
                .build();

        Trip savedTrip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        savedTrip.setId(10L);

        given(securityService.getCurrentUser()).willReturn(owner);
        given(itineraryRepository.findById(5L)).willReturn(Optional.of(existing));
        given(tripRepository.save(any(Trip.class))).willReturn(savedTrip);

        // when
        TripResponse response = tripService.createTrip(request);

        // then
        assertThat(response).isNotNull();
        assertThat(existing.getOwnerEmail()).isEqualTo("owner@test.com");
        assertThat(existing.getLocation()).isEqualTo("Jeju");
        assertThat(existing.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(existing.getEndDate()).isEqualTo(LocalDate.of(2026, 8, 5));
        verify(packingService).initDefaultItems(10L);
    }

    @Test
    @DisplayName("내 여행 목록 최신순 조회 성공")
    void getMyTrips_Success() {
        // given
        String email = "owner@test.com";
        User owner = User.builder().id(1L).email(email).name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(10L);

        given(securityService.getCurrentUserEmail()).willReturn(email);
        given(tripRepository.findByOwnerEmailOrderByCreatedAtDesc(email)).willReturn(List.of(trip));

        // when
        List<TripResponse> responses = tripService.getMyTrips();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Jeju Summer Trip");
    }

    @Test
    @DisplayName("여행 단건 조회 성공")
    void getTrip_Success() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);
        given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));

        // when
        TripResponse response = tripService.getTrip(tripId);

        // then
        assertThat(response.getId()).isEqualTo(tripId);
        assertThat(response.getDestination()).isEqualTo("Jeju");
    }

    @Test
    @DisplayName("여행 정보 전체 수정 성공 - 연결된 일정표 정보도 함께 갱신")
    void updateTrip_AllFields_Success() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Itinerary itinerary = Itinerary.builder().id(5L).title("Jeju Summer Trip 일정표").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);
        trip.setItinerary(itinerary);

        TripRequest request = TripRequest.builder()
                .title("  Busan Winter Trip  ")
                .destination("Busan")
                .country("Korea")
                .startDate(LocalDate.of(2026, 12, 1))
                .endDate(LocalDate.of(2026, 12, 5))
                .bgImageUrl("bg.png")
                .status("COMPLETED")
                .build();

        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");
        given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));
        given(tripRepository.save(any(Trip.class))).willReturn(trip);

        // when
        TripResponse response = tripService.updateTrip(tripId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(trip.getTitle()).isEqualTo("Busan Winter Trip");
        assertThat(trip.getDestination()).isEqualTo("Busan");
        assertThat(trip.getCountry()).isEqualTo("Korea");
        assertThat(trip.getBgImageUrl()).isEqualTo("bg.png");
        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(itinerary.getTitle()).isEqualTo("Busan Winter Trip 일정표");
        assertThat(itinerary.getLocation()).isEqualTo("Busan");
        assertThat(itinerary.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(itinerary.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 5));
    }

    @Test
    @DisplayName("연결된 일정표가 없는 여행 수정 시에도 정상 처리")
    void updateTrip_WithoutItinerary_Success() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);

        TripRequest request = TripRequest.builder()
                .title("Busan Winter Trip")
                .destination("Busan")
                .startDate(LocalDate.of(2026, 12, 1))
                .endDate(LocalDate.of(2026, 12, 5))
                .build();

        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");
        given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));
        given(tripRepository.save(any(Trip.class))).willReturn(trip);

        // when
        TripResponse response = tripService.updateTrip(tripId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(trip.getItinerary()).isNull();
        assertThat(trip.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 1));
    }

    @Test
    @DisplayName("본인 소유가 아닌 여행 수정 시 예외 발생")
    void updateTrip_NotOwner_ThrowsException() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);

        given(securityService.getCurrentUserEmail()).willReturn("stranger@test.com");
        given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));

        // when & then
        assertThatThrownBy(() -> tripService.updateTrip(tripId, TripRequest.builder().build()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인의 여행만 관리할 수 있습니다.");
    }

    @Test
    @DisplayName("여행 삭제 성공")
    void deleteTrip_Success() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);

        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");
        given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));

        // when
        tripService.deleteTrip(tripId);

        // then
        verify(tripRepository).delete(trip);
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID 조회 시 예외 발생")
    void getTrip_NotFound_ThrowsException() {
        // given
        given(tripRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tripService.getTrip(99L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("추천 여정 조회 시 목적지가 일치하는 공개 일정만 반환")
    void getRecommendedItineraries_ReturnsMatchingPublicItineraries() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", "Jeju", "Korea", owner);
        trip.setId(tripId);

        Itinerary matching = Itinerary.builder()
                .id(1L).title("제주 3박4일").location("Jeju Island").publicStatus(true).build();
        Itinerary privateOne = Itinerary.builder()
                .id(2L).title("비공개 제주").location("Jeju Island").publicStatus(false).build();
        Itinerary otherLocation = Itinerary.builder()
                .id(3L).title("부산 여행").location("Busan").publicStatus(true).build();
        Itinerary noLocation = Itinerary.builder()
                .id(4L).title("장소 미정").publicStatus(true).build();

        given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));
        given(itineraryRepository.findAll()).willReturn(List.of(matching, privateOne, otherLocation, noLocation));

        // when
        List<com.example.demo.dto.ItineraryResponse> responses = tripService.getRecommendedItineraries(tripId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("여행 목적지가 null인 경우 추천 여정 목록 조회 시 빈 리스트 반환")
    void getRecommendedItineraries_NullDestination_ReturnsEmptyList() {
        // given
        Long tripId = 10L;
        User owner = User.builder().id(1L).email("owner@test.com").name("Hong").build();
        Trip trip = Trip.of("Jeju Summer Trip", null, "Korea", owner);
        trip.setId(tripId);

        given(tripRepository.findById(tripId)).willReturn(Optional.of(trip));

        // when
        List<com.example.demo.dto.ItineraryResponse> responses = tripService.getRecommendedItineraries(tripId);

        // then
        assertThat(responses).isEmpty();
    }
}
