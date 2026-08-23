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
}
