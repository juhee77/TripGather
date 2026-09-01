package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.domain.User;
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

    @Mock
    private ProfanityFilterService profanityFilterService;

    @InjectMocks
    private ItineraryServiceImpl itineraryService;

    @Test
    @DisplayName("일정 생성 - RoutePoints와 함께 생성")
    void createItinerary_WithRoutePoints() {
        // given
        Itinerary itinerary = Itinerary.builder().title("With Points").build();
        com.example.demo.domain.RoutePoint rp = com.example.demo.domain.RoutePoint.builder().label("P1").dayNumber(1).build();
        itinerary.setRoutePoints(new ArrayList<>(java.util.List.of(rp)));
        
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary saved = itineraryService.createItinerary(itinerary);

        // then
        assertThat(saved.getRoutePoints().get(0).getItinerary()).isEqualTo(saved);
        verify(itineraryRepository).save(itinerary);
    }

    @Test
    @DisplayName("공개 여정 목록 조회 - 소프트 삭제되지 않은 항목만 반환")
    void getPublicItineraries_Success() {
        // given
        Itinerary i1 = Itinerary.builder().id(1L).publicStatus(true).build();
        given(itineraryRepository.findByPublicStatusTrueAndDeletedFalseOrderByCreatedAtDesc()).willReturn(java.util.List.of(i1));

        // when
        java.util.List<Itinerary> list = itineraryService.getPublicItineraries();

        // then
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("유저 소유 여정 목록 조회 - 소프트 삭제되지 않은 항목만 반환")
    void getUserJourneys_Success() {
        // given
        String email = "owner@test.com";
        Itinerary i1 = Itinerary.builder().id(2L).ownerEmail(email).build();
        given(itineraryRepository.findByOwnerEmailAndDeletedFalseOrderByCreatedAtDesc(email)).willReturn(java.util.List.of(i1));

        // when
        java.util.List<Itinerary> list = itineraryService.getUserJourneys(email);

        // then
        assertThat(list).hasSize(1);
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
                .ownerEmail("originalOwner@ex.com")
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
    @DisplayName("소프트 삭제된 원본 여정 복제 시 예외 발생")
    void cloneItinerary_DeletedOriginal_ThrowsException() {
        // given
        Long originalId = 1L;
        Itinerary original = Itinerary.builder().id(originalId).title("Deleted Trip").build();
        original.setDeleted(true);

        given(itineraryRepository.findById(originalId)).willReturn(Optional.of(original));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.cloneItinerary(originalId, "newOwner@ex.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("본인의 여정 복제 시도 시 예외 발생")
    void cloneItinerary_OwnItinerary_ThrowsException() {
        // given
        Long originalId = 1L;
        String ownerEmail = "me@ex.com";
        Itinerary original = Itinerary.builder()
                .id(originalId)
                .title("Original")
                .ownerEmail(ownerEmail)
                .build();

        given(itineraryRepository.findById(originalId)).willReturn(Optional.of(original));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.cloneItinerary(originalId, ownerEmail))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("본인의 여정은 복제할 수 없습니다.");
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
        Itinerary target = Itinerary.builder().id(tId).ownerEmail("owner@test.com").routePoints(new ArrayList<>(java.util.List.of(tPoint))).build();

        given(itineraryRepository.findById(sId)).willReturn(Optional.of(source));
        given(itineraryRepository.findById(tId)).willReturn(Optional.of(target));
        given(itineraryRepository.save(any(Itinerary.class))).willAnswer(i -> i.getArgument(0));

        // when
        Itinerary result = itineraryService.mergeItinerary(sId, tId, 1, "owner@test.com");

        // then
        assertThat(result.getRoutePoints()).hasSize(2);
        assertThat(result.getRoutePoints().get(1).getLabel()).isEqualTo("Source P");
        assertThat(result.getRoutePoints().get(1).getSequenceOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("경로 지점 체크인 완료 및 포인트 보상 지급 검증")
    void toggleRoutePointCompletion_Success() {
        // given
        Long itineraryId = 1L;
        Long pointId = 10L;
        String email = "test@example.com";
        User user = User.builder().id(100L).email(email).build();

        com.example.demo.domain.RoutePoint point = com.example.demo.domain.RoutePoint.builder()
                .id(pointId)
                .label("Haeundae Beach")
                .isCompleted(false)
                .build();

        Itinerary itinerary = Itinerary.builder()
                .id(itineraryId)
                .routePoints(new ArrayList<>(java.util.List.of(point)))
                .build();

        given(itineraryRepository.findById(itineraryId)).willReturn(Optional.of(itinerary));
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        com.example.demo.domain.RoutePoint result = itineraryService.toggleRoutePointCompletion(itineraryId, pointId, email);

        // then
        assertThat(result.isCompleted()).isTrue();
        verify(pointService).addPoints(100L, 20, 0, "'Haeundae Beach' 체크인 완료");
    }

    @Test
    @DisplayName("여정 생성 시 제목에 비속어 포함 시 예외 발생")
    void createItinerary_ProfanityTitle_ThrowsException() {
        // given
        Itinerary itinerary = Itinerary.builder().title("개새끼 부산 여행").build();
        org.mockito.BDDMockito.willThrow(new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText("개새끼 부산 여행");

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.createItinerary(itinerary))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("경로 포인트 체크인 시 라벨에 비속어 포함 시 예외 발생")
    void toggleRoutePointCompletion_ProfanityLabel_ThrowsException() {
        // given
        Long itineraryId = 1L;
        Long pointId = 10L;
        String profanityLabel = "씨발해변";

        com.example.demo.domain.RoutePoint point = com.example.demo.domain.RoutePoint.builder()
                .id(pointId)
                .label(profanityLabel)
                .isCompleted(false)
                .build();

        Itinerary itinerary = Itinerary.builder()
                .id(itineraryId)
                .routePoints(new ArrayList<>(java.util.List.of(point)))
                .build();

        given(itineraryRepository.findById(itineraryId)).willReturn(Optional.of(itinerary));
        org.mockito.BDDMockito.willThrow(new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText(profanityLabel);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                itineraryService.toggleRoutePointCompletion(itineraryId, pointId, "user@test.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("여정 생성 시 경로 포인트 장소명이 공백인 경우 예외 발생")
    void createItinerary_EmptyRoutePointLabel_ThrowsException() {
        // given
        com.example.demo.domain.RoutePoint point = com.example.demo.domain.RoutePoint.builder()
                .label("   ")
                .dayNumber(1)
                .build();

        Itinerary itinerary = Itinerary.builder()
                .title("Valid Title")
                .routePoints(new ArrayList<>(java.util.List.of(point)))
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.createItinerary(itinerary))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("경로 포인트 장소명을 입력해주세요.");
    }

    @Test
    @DisplayName("여정 수정 시 경로 포인트 장소명이 공백인 경우 예외 발생")
    void updateItinerary_EmptyRoutePointLabel_ThrowsException() {
        // given
        Itinerary existing = Itinerary.builder().id(1L).title("Old Title").routePoints(new ArrayList<>()).build();
        given(itineraryRepository.findById(1L)).willReturn(Optional.of(existing));

        com.example.demo.domain.RoutePoint invalidPoint = com.example.demo.domain.RoutePoint.builder()
                .label("   ")
                .dayNumber(1)
                .build();

        Itinerary updateInfo = Itinerary.builder()
                .title("New Title")
                .routePoints(new ArrayList<>(java.util.List.of(invalidPoint)))
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.updateItinerary(1L, updateInfo))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("경로 포인트 장소명을 입력해주세요.");
    }

    @Test
    @DisplayName("여정 수정 시 제목이 공백인 경우 예외 발생")
    void updateItinerary_EmptyTitle_ThrowsException() {
        // given
        Itinerary existing = Itinerary.builder().id(1L).title("Old Title").routePoints(new ArrayList<>()).build();
        given(itineraryRepository.findById(1L)).willReturn(Optional.of(existing));

        Itinerary updateInfo = Itinerary.builder()
                .title("   ")
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.updateItinerary(1L, updateInfo))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여정 제목을 입력해주세요.");
    }

    @Test
    @DisplayName("여정 생성 시 경로 순서가 0 미만 음수인 경우 예외 발생")
    void createItinerary_NegativeSequenceOrder_ThrowsException() {
        // given
        com.example.demo.domain.RoutePoint point = com.example.demo.domain.RoutePoint.builder()
                .label("Valid Location")
                .dayNumber(1)
                .sequenceOrder(-1)
                .build();

        Itinerary itinerary = Itinerary.builder()
                .title("Valid Title")
                .routePoints(new ArrayList<>(java.util.List.of(point)))
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.createItinerary(itinerary))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("경로 순서는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("여정 생성 시 일차 번호가 0 이하인 경우 예외 발생")
    void createItinerary_ZeroDayNumber_ThrowsException() {
        // given
        com.example.demo.domain.RoutePoint point = com.example.demo.domain.RoutePoint.builder()
                .label("Valid Location")
                .dayNumber(0)
                .sequenceOrder(0)
                .build();

        Itinerary itinerary = Itinerary.builder()
                .title("Valid Title")
                .routePoints(new ArrayList<>(java.util.List.of(point)))
                .build();

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.createItinerary(itinerary))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("일차 번호는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 여정 삭제 시 예외 발생")
    void deleteItinerary_NotFound_ThrowsException() {
        // given
        Long itineraryId = 99L;
        given(itineraryRepository.existsById(itineraryId)).willReturn(false);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.deleteItinerary(itineraryId))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여정을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("여정 삭제 성공")
    void deleteItinerary_Success() {
        // given
        Long itineraryId = 1L;
        given(itineraryRepository.existsById(itineraryId)).willReturn(true);

        // when
        itineraryService.deleteItinerary(itineraryId);

        // then
        verify(itineraryRepository).softDeleteById(itineraryId);
    }

    @Test
    @DisplayName("존재하지 않는 여정 수정 시 예외 발생")
    void updateItinerary_NotFound_ThrowsException() {
        // given
        Long itineraryId = 99L;
        given(itineraryRepository.findById(itineraryId)).willReturn(Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.updateItinerary(itineraryId, new Itinerary()))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("존재하지 않는 여정의 경로 포인트 토글 완료 처리 시 예외 발생")
    void toggleRoutePointCompletion_ItineraryNotFound_ThrowsException() {
        // given
        Long itineraryId = 99L;
        given(itineraryRepository.findById(itineraryId)).willReturn(Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.toggleRoutePointCompletion(itineraryId, 10L, "user@test.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("동일한 여정 ID 병합 시도 시 예외 발생")
    void mergeItinerary_SameItinerary_ThrowsException() {
        // given
        Long itineraryId = 1L;

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.mergeItinerary(itineraryId, itineraryId, 1, "owner@test.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("자기 자신 여정과는 병합할 수 없습니다.");
    }

    @Test
    @DisplayName("여정 내 존재하지 않는 경로 포인트 ID 토글 완료 처리 시 예외 발생")
    void toggleRoutePointCompletion_PointNotFound_ThrowsException() {
        // given
        Long itineraryId = 1L;
        Itinerary itinerary = Itinerary.builder().id(itineraryId).routePoints(new ArrayList<>()).build();
        given(itineraryRepository.findById(itineraryId)).willReturn(Optional.of(itinerary));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.toggleRoutePointCompletion(itineraryId, 999L, "user@test.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("경로 포인트를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("타인 소유 여정으로 병합 시도 시 권한 예외 발생")
    void mergeItinerary_NotOwner_ThrowsForbidden() {
        // given
        Long sId = 1L;
        Long tId = 2L;
        Itinerary source = Itinerary.builder().id(sId).routePoints(new ArrayList<>()).build();
        Itinerary target = Itinerary.builder().id(tId).ownerEmail("owner@test.com").routePoints(new ArrayList<>()).build();

        given(itineraryRepository.findById(sId)).willReturn(Optional.of(source));
        given(itineraryRepository.findById(tId)).willReturn(Optional.of(target));

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> itineraryService.mergeItinerary(sId, tId, 1, "hacker@test.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("여정 병합 시 0명 이하 일차 번호(targetDay) 전달 시 예외 발생")
    void mergeItinerary_InvalidTargetDay_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.mergeItinerary(1L, 2L, 0, "owner@test.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("병합 대상 일차 번호는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("여정 공개 상태 토글 시 공백 또는 null 이메일 전달 시 예외 발생")
    void togglePublicStatus_EmptyEmail_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> itineraryService.togglePublicStatus(1L, "   ", true))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("유저 이메일 정보가 올바르지 않습니다.");
    }
}
