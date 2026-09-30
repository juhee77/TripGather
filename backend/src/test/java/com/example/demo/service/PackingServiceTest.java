package com.example.demo.service;

import com.example.demo.domain.PackingItem;
import com.example.demo.domain.Trip;
import com.example.demo.dto.PackingProgressResponse;
import com.example.demo.repository.PackingItemRepository;
import com.example.demo.repository.TripRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PackingServiceTest {

    @Mock
    private PackingItemRepository packingItemRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ProfanityFilterService profanityFilterService;

    @Mock
    private TripAccessGuard tripAccessGuard;

    @InjectMocks
    private PackingService packingService;

    @Test
    @DisplayName("준비물 진행률 계산 성공 - 2개 중 1개 완료 시 50%")
    void getPackingProgress_Success() {
        // given
        Long tripId = 1L;
        Trip trip = Trip.builder().id(tripId).title("Busan Trip").build();

        PackingItem item1 = PackingItem.of(trip, "Passport", "Essential");
        item1.setChecked(true);

        PackingItem item2 = PackingItem.of(trip, "Charger", "Tech");
        item2.setChecked(false);

        given(tripRepository.existsById(tripId)).willReturn(true);
        given(packingItemRepository.findByTripId(tripId)).willReturn(List.of(item1, item2));

        // when
        PackingProgressResponse response = packingService.getPackingProgress(tripId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getCheckedCount()).isEqualTo(1);
        assertThat(response.getProgressPercentage()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("준비물 추가 시 항목명에 비속어 포함 시 예외 발생")
    void addItem_ProfanityName_ThrowsException() {
        // given
        String profanityName = "시발약통";
        org.mockito.BDDMockito.willThrow(new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText(profanityName);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.addItem(1L, profanityName, "기타"))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("준비물 추가 시 공백 또는 빈 항목명 입력 시 예외 발생")
    void addItem_EmptyName_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.addItem(1L, "   ", "기타"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("준비물 항목명을 입력해주세요.");
    }

    @Test
    @DisplayName("준비물 추가 시 카테고리에 비속어 포함 시 예외 발생")
    void addItem_ProfanityCategory_ThrowsException() {
        // given
        org.mockito.Mockito.doAnswer(invocation -> {
            String arg = invocation.getArgument(0);
            if ("씨발카테고리".equals(arg)) {
                throw new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다.");
            }
            return null;
        }).when(profanityFilterService).validateText(org.mockito.ArgumentMatchers.anyString());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.addItem(1L, "Passport", "씨발카테고리"))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("존재하지 않는 준비물 삭제 시 예외 발생")
    void deleteItem_NotFound_ThrowsException() {
        // given
        given(packingItemRepository.findById(99L)).willReturn(java.util.Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.deleteItem(99L))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("준비물을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("준비물 삭제 성공")
    void deleteItem_Success() {
        // given
        PackingItem item = PackingItem.builder().id(10L).name("Towel").build();
        given(packingItemRepository.findById(10L)).willReturn(java.util.Optional.of(item));

        // when
        packingService.deleteItem(10L);

        // then
        org.mockito.Mockito.verify(packingItemRepository).delete(item);
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 준비물 진행도 조회 시 예외 발생")
    void getPackingProgress_TripNotFound_ThrowsException() {
        // given
        Long tripId = 99L;
        given(tripRepository.existsById(tripId)).willReturn(false);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.getPackingProgress(tripId))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 준비물 체크 토글 시 예외 발생")
    void toggleCheck_NotFound_ThrowsException() {
        // given
        given(packingItemRepository.findById(99L)).willReturn(java.util.Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.toggleCheck(99L))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("준비물을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 준비물 추가 시 예외 발생")
    void addItem_TripNotFound_ThrowsException() {
        // given
        Long tripId = 99L;
        given(tripRepository.findById(tripId)).willReturn(java.util.Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.addItem(tripId, "Passport", "필수"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 기본 준비물 초기화 시 예외 발생")
    void initDefaultItems_TripNotFound_ThrowsException() {
        // given
        Long tripId = 99L;
        given(tripRepository.findById(tripId)).willReturn(java.util.Optional.empty());

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.initDefaultItems(tripId))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("준비물 체크 상태 토글 성공")
    void toggleCheck_Success() {
        // given
        Long itemId = 10L;
        PackingItem item = PackingItem.builder().id(itemId).name("Socks").checked(false).build();
        given(packingItemRepository.findById(itemId)).willReturn(java.util.Optional.of(item));
        given(packingItemRepository.save(item)).willReturn(item);

        // when
        com.example.demo.dto.PackingItemResponse response = packingService.toggleCheck(itemId);

        // then
        assertThat(response.isChecked()).isTrue();
    }

    @Test
    @DisplayName("null 여행 ID로 기본 준비물 초기화 시 예외 발생")
    void initDefaultItems_NullTripId_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.initDefaultItems(null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("null 여행 ID로 준비물 항목 추가 시 예외 발생")
    void addItem_NullTripId_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.addItem(null, "Towel", "세면"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("null 여행 ID로 준비물 진행도 조회 시 예외 발생")
    void getPackingProgress_NullTripId_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.getPackingProgress(null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("null 준비물 ID로 항목 삭제 시 예외 발생")
    void deleteItem_NullItemId_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.deleteItem(null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("준비물 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("null 준비물 ID로 체크 상태 토글 시 예외 발생")
    void toggleCheck_NullItemId_ThrowsException() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.toggleCheck(null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("준비물 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("null 여행 ID로 준비물 목록 조회 시 예외 발생")
    void getItems_NullTripId_ThrowsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.getItems(null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 여행 ID로 준비물 목록 조회 시 예외 발생")
    void getItems_TripNotFound_ThrowsException() {
        given(tripRepository.existsById(99L)).willReturn(false);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.getItems(99L))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("준비물 항목명이 100자를 초과하는 경우 예외 발생")
    void addItem_ExceedNameLength_ThrowsException() {
        String longName = "A".repeat(101);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.addItem(1L, longName, "기타"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("준비물 항목명은 100자 이내여야 합니다.");
    }

    @Test
    @DisplayName("카테고리명이 50자를 초과하는 경우 예외 발생")
    void addItem_ExceedCategoryLength_ThrowsException() {
        String longCat = "C".repeat(51);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> packingService.addItem(1L, "Towel", longCat))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("카테고리명은 50자 이내여야 합니다.");
    }

    @org.junit.jupiter.api.Nested
    @DisplayName("기본 준비물 템플릿")
    class DefaultTemplate {

        private Trip trip(String country, java.time.LocalDate start, java.time.LocalDate end) {
            return Trip.builder().id(1L).title("여행").country(country)
                    .startDate(start).endDate(end).build();
        }

        /** 템플릿 생성 후 저장된 항목 이름을 모은다. */
        private java.util.List<String> savedNames(Trip trip, java.util.List<PackingItem> alreadyThere) {
            given(tripRepository.findById(1L)).willReturn(java.util.Optional.of(trip));
            given(tripRepository.existsById(1L)).willReturn(true);
            given(packingItemRepository.findByTripId(1L)).willReturn(alreadyThere);
            given(packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(1L)).willReturn(List.of());

            packingService.initDefaultItems(1L);

            org.mockito.ArgumentCaptor<PackingItem> captor =
                    org.mockito.ArgumentCaptor.forClass(PackingItem.class);
            org.mockito.Mockito.verify(packingItemRepository, org.mockito.Mockito.atLeast(0))
                    .save(captor.capture());
            return captor.getAllValues().stream().map(PackingItem::getName).toList();
        }

        @Test
        @DisplayName("국내 여행에는 여권과 멀티어댑터를 넣지 않는다")
        void domesticTripSkipsOverseasItems() {
            java.util.List<String> names = savedNames(
                    trip("KR", java.time.LocalDate.of(2026, 5, 1), java.time.LocalDate.of(2026, 5, 3)),
                    List.of());

            assertThat(names).doesNotContain("여권", "멀티어댑터", "여행자 보험");
            assertThat(names).contains("현금/카드", "상비약");
        }

        @Test
        @DisplayName("해외 여행에는 여권과 멀티어댑터를 넣는다")
        void overseasTripIncludesOverseasItems() {
            java.util.List<String> names = savedNames(
                    trip("JP", java.time.LocalDate.of(2026, 5, 1), java.time.LocalDate.of(2026, 5, 3)),
                    List.of());

            assertThat(names).contains("여권", "멀티어댑터", "여행자 보험");
        }

        @Test
        @DisplayName("당일치기에는 잠옷과 숙소 예약 확인서를 넣지 않는다")
        void dayTripSkipsOvernightItems() {
            java.time.LocalDate sameDay = java.time.LocalDate.of(2026, 5, 1);
            java.util.List<String> names = savedNames(trip("KR", sameDay, sameDay), List.of());

            assertThat(names).doesNotContain("잠옷", "숙소 예약 확인서", "샴푸/바디워시");
            assertThat(names).contains("외투", "현금/카드");
        }

        @Test
        @DisplayName("일수만큼 필요한 항목에는 며칠치인지 적어 준다")
        void perDayItemCarriesDayCount() {
            // 5/1 ~ 5/3 은 2박 3일
            java.util.List<String> names = savedNames(
                    trip("KR", java.time.LocalDate.of(2026, 5, 1), java.time.LocalDate.of(2026, 5, 3)),
                    List.of());

            assertThat(names).contains("속옷/양말 (3일치)");
        }

        @Test
        @DisplayName("날짜를 모르면 숙박 여부를 단정하지 않고 모두 넣는다")
        void unknownDatesKeepOvernightItems() {
            java.util.List<String> names = savedNames(trip("KR", null, null), List.of());

            assertThat(names).contains("잠옷", "숙소 예약 확인서");
            // 며칠치인지도 알 수 없으므로 수량을 붙이지 않는다
            assertThat(names).contains("속옷/양말");
        }

        @Test
        @DisplayName("이미 있는 항목은 다시 만들지 않는다")
        void doesNotDuplicateExistingItems() {
            // 예전에는 버튼을 두 번 누르면 목록이 통째로 두 벌이 됐다.
            Trip trip = trip("KR", java.time.LocalDate.of(2026, 5, 1), java.time.LocalDate.of(2026, 5, 3));
            java.util.List<PackingItem> already = List.of(
                    PackingItem.of(trip, "현금/카드", "필수"),
                    PackingItem.of(trip, "상비약", "기타"));

            java.util.List<String> names = savedNames(trip, already);

            assertThat(names).doesNotContain("현금/카드", "상비약");
            assertThat(names).contains("외투");
        }
    }

    @Test
    @DisplayName("준비물 목록은 필수 카테고리부터 보여 준다")
    void getItems_OrdersEssentialCategoryFirst() {
        // 이름순으로 두면 유니코드 차례상 "기타" 가 맨 위, "필수" 가 맨 아래로 간다.
        Trip trip = Trip.builder().id(1L).title("여행").build();
        given(tripRepository.existsById(1L)).willReturn(true);
        given(packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(1L)).willReturn(List.of(
                PackingItem.of(trip, "우산", "기타"),
                PackingItem.of(trip, "썬크림", "세면"),
                PackingItem.of(trip, "여권", "필수"),
                PackingItem.of(trip, "충전기", "전자기기"),
                PackingItem.of(trip, "내가 만든 것", "나만의 분류")));

        java.util.List<String> categories = packingService.getItems(1L).stream()
                .map(com.example.demo.dto.PackingItemResponse::getCategory)
                .toList();

        assertThat(categories).containsExactly("필수", "전자기기", "세면", "기타", "나만의 분류");
    }
}
