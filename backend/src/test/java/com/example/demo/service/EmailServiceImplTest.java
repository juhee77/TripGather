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
    @DisplayName("인증 링크는 설정된 서버 주소를 쓴다")
    void sendVerificationEmail_UsesConfiguredBackendUrl() {
        // 예전에는 "http://localhost:8080/api/auth" 가 상수로 박혀 있었다.
        // 발송 여부만 확인하는 테스트뿐이어서 링크가 로컬을 가리켜도 드러나지 않았고,
        // 배포하면 받는 사람이 열 수 없는 주소가 메일에 담긴다.
        org.springframework.test.util.ReflectionTestUtils.setField(
                emailService, "backendUrl", "https://api.tripgather.com");

        emailService.sendVerificationEmail("user@example.com", "tok-123");

        org.mockito.ArgumentCaptor<SimpleMailMessage> captor =
                org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertThat(body).contains("https://api.tripgather.com/api/auth/verify?token=tok-123");
        assertThat(body).doesNotContain("localhost");
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 테스트 (미구현)")
    void sendPasswordResetEmail_NotImplemented() {
        // when & then
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "token"));
    }
}
