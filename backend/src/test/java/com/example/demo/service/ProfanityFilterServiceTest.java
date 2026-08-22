package com.example.demo.service;

import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProfanityFilterServiceTest {

    private final ProfanityFilterService profanityFilterService = new ProfanityFilterService();

    @Test
    @DisplayName("정상적인 텍스트 검증 통과")
    void validateText_ValidContent_Passes() {
        assertDoesNotThrow(() -> profanityFilterService.validateText("즐거운 해운대 모임입니다!"));
    }

    @Test
    @DisplayName("비속어 포함 텍스트 검증 시 예외 발생")
    void validateText_ProfanityContent_ThrowsException() {
        assertThatThrownBy(() -> profanityFilterService.validateText("이 모임 진짜 개새끼 같네요"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }
}
