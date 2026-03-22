package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        // Auth failure (wrong email/password) uses IllegalArgumentException in AuthService
        if (e.getMessage().contains("비밀번호") || e.getMessage().contains("이메일")) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
        return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        if (e.getMessage().contains("Authentication")) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        }
        return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("An unexpected error occurred: " + e.getMessage()));
    }
}
