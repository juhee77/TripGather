package com.example.demo.service;

import com.example.demo.domain.Trip;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.TripRepository;
import com.example.demo.security.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("여행 소유권 검사")
class TripAccessGuardTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TripAccessGuard tripAccessGuard;

    private Trip tripOwnedBy(String email) {
        User owner = User.builder().id(1L).email(email).build();
        Trip trip = Trip.of("여행", "부산", "Korea", owner);
        trip.setId(1L);
        return trip;
    }

    @Test
    @DisplayName("소유자는 통과한다")
    void requireOwner_Owner_Passes() {
        given(tripRepository.findById(1L)).willReturn(Optional.of(tripOwnedBy("owner@test.com")));
        given(securityService.isAnonymous()).willReturn(false);
        given(securityService.getCurrentUserEmail()).willReturn("owner@test.com");

        assertThat(tripAccessGuard.requireOwner(1L).getTitle()).isEqualTo("여행");
    }

    @Test
    @DisplayName("다른 사용자는 차단된다")
    void requireOwner_OtherUser_Throws() {
        given(tripRepository.findById(1L)).willReturn(Optional.of(tripOwnedBy("owner@test.com")));
        given(securityService.isAnonymous()).willReturn(false);
        given(securityService.getCurrentUserEmail()).willReturn("stranger@test.com");

        assertThatThrownBy(() -> tripAccessGuard.requireOwner(1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인의 여행만 조회할 수 있습니다.");
    }

    @Test
    @DisplayName("비로그인 사용자는 차단된다")
    void requireOwner_Anonymous_Throws() {
        given(tripRepository.findById(1L)).willReturn(Optional.of(tripOwnedBy("owner@test.com")));
        given(securityService.isAnonymous()).willReturn(true);

        assertThatThrownBy(() -> tripAccessGuard.requireOwner(1L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("null 여행 ID 는 예외")
    void requireOwner_NullId_Throws() {
        assertThatThrownBy(() -> tripAccessGuard.requireOwner(null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 여행은 예외")
    void requireOwner_NotFound_Throws() {
        given(tripRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tripAccessGuard.requireOwner(99L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }
}
