package com.example.demo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("CustomException 처리 시 ErrorCode의 상태코드와 호출부 상세 메시지를 그대로 응답")
    void handleCustomException_UsesDetailMessage() {
        // given
        CustomException exception = new CustomException(ErrorCode.FORBIDDEN_ACTION, "본인의 여행만 관리할 수 있습니다.");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleCustomException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FORBIDDEN_ACTION");
        assertThat(response.getBody().getMessage()).isEqualTo("본인의 여행만 관리할 수 있습니다.");
    }

    @Test
    @DisplayName("검증 실패 시 필드명과 사유를 조합한 메시지로 400 응답")
    void handleMethodArgumentNotValid_JoinsFieldErrors() {
        // given
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "tripRequest");
        bindingResult.addError(new FieldError("tripRequest", "title", "여행 제목은 필수입니다."));
        bindingResult.addError(new FieldError("tripRequest", "rating", "평점은 1 이상이어야 합니다."));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        given(exception.getBindingResult()).willReturn(bindingResult);

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage())
                .isEqualTo("title: 여행 제목은 필수입니다., rating: 평점은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("필수 요청 파라미터 누락 시 기본 메시지로 400 응답")
    void handleBadRequest_MissingParameter() {
        // given
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("tripId", "Long");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequest(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_INPUT_VALUE");
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @Test
    @DisplayName("요청 본문 파싱 실패 시 기본 메시지로 400 응답")
    void handleBadRequest_UnreadableMessage() {
        // given
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("JSON parse error", (org.springframework.http.HttpInputMessage) null);

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequest(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_INPUT_VALUE");
    }

    @Test
    @DisplayName("권한 없는 접근 시 403 응답")
    void handleAccessDenied_Returns403() {
        // given
        AccessDeniedException exception = new AccessDeniedException("접근이 거부되었습니다.");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDenied(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FORBIDDEN_ACTION");
    }

    @Test
    @DisplayName("IllegalArgumentException 처리 시 예외 메시지를 그대로 400 응답에 노출")
    void handleIllegalArgumentException_ExposesMessage() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("존재하지 않는 상태 값입니다.");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("존재하지 않는 상태 값입니다.");
    }

    @Test
    @DisplayName("처리되지 않은 예외는 내부 메시지를 노출하지 않고 500 기본 메시지로 응답")
    void handleException_DoesNotLeakInternalMessage() {
        // given
        Exception exception = new RuntimeException("DB 커넥션 풀 고갈: jdbc:postgresql://prod-db:5432");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        assertThat(response.getBody().getMessage()).doesNotContain("prod-db");
    }
}
