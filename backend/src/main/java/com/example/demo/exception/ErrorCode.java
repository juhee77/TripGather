package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력 값입니다."),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),

    SELF_ACTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SELF_ACTION_NOT_ALLOWED", "호스트 본인을 대상으로 한 작업은 허용되지 않습니다."),

    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_ACCESS", "유효하지 않은 인증 정보입니다."),

    FORBIDDEN_ACTION(HttpStatus.FORBIDDEN, "FORBIDDEN_ACTION", "해당 작업을 수행할 권한이 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    ITINERARY_NOT_FOUND(HttpStatus.NOT_FOUND, "ITINERARY_NOT_FOUND", "해당 여정을 찾을 수 없습니다."),

    GATHERING_NOT_FOUND(HttpStatus.NOT_FOUND, "GATHERING_NOT_FOUND", "해당 모임을 찾을 수 없습니다."),
    MEMBER_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_REQUEST_NOT_FOUND", "해당 멤버 요청을 찾을 수 없습니다."),
    DM_NOT_FOUND(HttpStatus.NOT_FOUND, "DM_NOT_FOUND", "해당 메시지를 찾을 수 없습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
