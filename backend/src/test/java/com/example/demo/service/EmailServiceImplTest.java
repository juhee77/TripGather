package com.example.demo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    @DisplayName("인증 이메일 발송 성공 테스트")
    void sendVerificationEmail_Success() {
        // given
        String to = "user@example.com";
        String token = "sample-token-123";

        // when & then
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(to, token));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 테스트 (미구현)")
    void sendPasswordResetEmail_NotImplemented() {
        // when & then
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "token"));
    }
}
