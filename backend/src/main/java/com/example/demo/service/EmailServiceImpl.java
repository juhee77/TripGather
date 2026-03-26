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
    private final String BASE_URL = "http://localhost:8080/api/auth"; // Should be in .env

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = BASE_URL + "/verify?token=" + token;
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
