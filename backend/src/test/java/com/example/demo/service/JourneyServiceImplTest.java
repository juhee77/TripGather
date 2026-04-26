package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.domain.JourneyEntry;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.JourneyEntryRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JourneyServiceImplTest {

    @Mock
    private JourneyEntryRepository journeyEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItineraryRepository itineraryRepository;

    @InjectMocks
    private JourneyServiceImpl journeyService;

    @Test
    @DisplayName("나의 여정 목록 조회 성공")
    void getMyJourneys_Success() {
        // given
        User user = User.builder().id(1L).email("user@example.com").build();
        Itinerary itinerary = Itinerary.builder().id(100L).build();
        JourneyEntry entry = JourneyEntry.builder().itinerary(itinerary).build();

        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(journeyEntryRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())).willReturn(List.of(entry));

        // when
        List<Itinerary> results = journeyService.getMyJourneys("user@example.com");

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("여정에 추가 성공 - 기존에 없는 경우")
    void addToMyJourney_NewEntry_Success() {
        // given
        User user = User.builder().id(1L).email("user@example.com").build();
        Itinerary itinerary = Itinerary.builder().id(100L).build();

        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(itineraryRepository.findById(100L)).willReturn(Optional.of(itinerary));
        given(journeyEntryRepository.findByUserIdAndItineraryId(user.getId(), 100L)).willReturn(Optional.empty());

        // when
        Itinerary result = journeyService.addToMyJourney(100L, "user@example.com");

        // then
        assertThat(result.getId()).isEqualTo(100L);
        verify(journeyEntryRepository).save(any(JourneyEntry.class));
    }

    @Test
    @DisplayName("여정에 추가 시 - 이미 존재하는 경우 추가하지 않음")
    void addToMyJourney_AlreadyExists_NoNewSave() {
        // given
        User user = User.builder().id(1L).email("user@example.com").build();
        Itinerary itinerary = Itinerary.builder().id(100L).build();
        JourneyEntry existingEntry = JourneyEntry.builder().user(user).itinerary(itinerary).build();

        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(itineraryRepository.findById(100L)).willReturn(Optional.of(itinerary));
        given(journeyEntryRepository.findByUserIdAndItineraryId(user.getId(), 100L)).willReturn(Optional.of(existingEntry));

        // when
        journeyService.addToMyJourney(100L, "user@example.com");

        // then
        verify(journeyEntryRepository, never()).save(any(JourneyEntry.class));
    }

    @Test
    @DisplayName("여정에서 제거 성공")
    void removeFromMyJourney_Success() {
        // given
        User user = User.builder().id(1L).email("user@example.com").build();
        JourneyEntry entry = JourneyEntry.builder().id(50L).user(user).build();

        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(journeyEntryRepository.findByUserIdAndItineraryId(user.getId(), 100L)).willReturn(Optional.of(entry));

        // when
        journeyService.removeFromMyJourney(100L, "user@example.com");

        // then
        verify(journeyEntryRepository).delete(entry);
    }

    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 예외 발생")
    void getUserByEmail_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail("none@ex.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> journeyService.getMyJourneys("none@ex.com"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("여정을 찾을 수 없는 경우 추가 시 예외 발생")
    void addToMyJourney_ItineraryNotFound_ThrowsException() {
        // given
        User user = User.builder().email("user@ex.com").build();
        given(userRepository.findByEmail("user@ex.com")).willReturn(Optional.of(user));
        given(itineraryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> journeyService.addToMyJourney(999L, "user@ex.com"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여정을 찾을 수 없습니다.");
    }
}
