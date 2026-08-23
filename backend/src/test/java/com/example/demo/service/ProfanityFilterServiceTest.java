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

    @Test
    @DisplayName("특수문자 및 공백을 섞어 우회 시도한 비속어 탐지 예외 발생")
    void validateText_ProfanityBypass_ThrowsException() {
        assertThatThrownBy(() -> profanityFilterService.validateText("개-새_끼 모임 f.u.c.k"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("동적 비속어 등록 후 해당 단어 포함 시 예외 발생")
    void addProfanityWord_DynamicRegistration_ThrowsExceptionOnValidation() {
        // given
        profanityFilterService.addProfanityWord("광고성링크");

        // when & then
        assertThatThrownBy(() -> profanityFilterService.validateText("여기 클릭 광고성링크 접속하세요"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }
}
