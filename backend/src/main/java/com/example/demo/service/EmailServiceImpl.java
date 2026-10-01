package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * 인증 링크에 쓸 서버 주소.
     *
     * 예전에는 "http://localhost:8080/api/auth" 가 상수로 박혀 있었다.
     * 받는 사람 컴퓨터에는 그런 서버가 없으므로 배포하면 아무도 가입을 끝낼 수 없다.
     * (가입 응답은 인증 전까지 토큰을 주지 않는다)
     * app.backend-url 은 프로필마다 이미 정의되어 있었는데 쓰는 곳이 없었다.
     */
    @org.springframework.beans.factory.annotation.Value("${app.backend-url}")
    private String backendUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = backendUrl + "/api/auth/verify?token=" + token;
        log.info("Sending verification email to {}. URL: {}", to, verificationUrl);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[TripGather] 이메일 인증을 완료해주세요");
        message.setText("아래 링크를 클릭하여 이메일 인증을 완료해주세요:\n" + verificationUrl);
        
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        // To be implemented in next step
    }
}
