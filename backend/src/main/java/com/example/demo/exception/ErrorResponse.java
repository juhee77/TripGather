package com.example.demo.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return toResponseEntity(errorCode, errorCode.getMessage());
    }

    /**
     * 호출부에서 전달한 상세 메시지를 그대로 노출한다.
     * (메시지가 비어 있으면 ErrorCode 의 기본 메시지로 대체)
     */
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String message) {
        String resolved = (message == null || message.isBlank()) ? errorCode.getMessage() : message;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(errorCode.getStatus().value())
                        .error(errorCode.getStatus().name())
                        .code(errorCode.getCode())
                        .message(resolved)
                        .build()
                );
    }
}
