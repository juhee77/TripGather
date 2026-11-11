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

    /** 재설정 화면은 프론트엔드에 있다. */
    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = backendUrl + "/api/auth/verify?token=" + token;
        log.info("Sending verification email to {}. URL: {}", to, verificationUrl);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[TripGather] 이메일 인증을 완료해주세요");
        message.setText("아래 링크를 클릭하여 이메일 인증을 완료해주세요:\n" + verificationUrl);

        send(message, verificationUrl);
    }

    /**
     * 비밀번호 재설정 링크를 보낸다.
     *
     * 인증 메일과 달리 백엔드가 아니라 프론트엔드 화면으로 보낸다.
     * 새 비밀번호를 입력받아야 하므로 사람이 보는 화면이 필요하기 때문이다.
     */
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        log.info("Sending password reset email to {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[TripGather] 비밀번호 재설정 안내");
        message.setText("아래 링크에서 새 비밀번호를 설정해주세요. 링크는 30분 동안만 유효합니다.\n"
                + resetUrl
                + "\n\n본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.");

        send(message, resetUrl);
    }

    /**
     * 메일 발송 실패가 본래 요청을 깨지 않게 한다.
     *
     * 가입이나 재설정 요청은 토큰을 이미 저장한 뒤에 메일을 보낸다.
     * 여기서 예외가 그대로 올라가면 사용자는 500 을 받고, 정작 토큰은 발급된
     * 어정쩡한 상태로 남는다. 로컬은 SMTP 자격증명이 placeholder 라 늘 실패하므로
     * 자체 가입을 시험해 볼 수도 없었다.
     *
     * 대신 링크를 로그에 남긴다. 로컬에서는 이 로그로 흐름을 이어서 확인할 수 있고,
     * 운영에서는 발송이 막혔다는 사실이 기록으로 남는다.
     */
    private void send(SimpleMailMessage message, String link) {
        try {
            mailSender.send(message);
        } catch (org.springframework.mail.MailException e) {
            log.error("메일 발송 실패 (to={}, link={}). 토큰은 이미 발급되었다.",
                    message.getTo() != null ? String.join(",", message.getTo()) : "?", link, e);
        }
    }
}
