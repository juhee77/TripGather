package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    // 사용자 이메일을 키로 하여 SseEmitter 관리
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final com.example.demo.repository.GatheringMemberRepository gatheringMemberRepository;

    public NotificationService(com.example.demo.repository.GatheringMemberRepository gatheringMemberRepository) {
        this.gatheringMemberRepository = gatheringMemberRepository;
    }

    public SseEmitter subscribe(String email) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60); // 1시간 타임아웃
        emitters.put(email, emitter);

        // 연결 종료 처리
        emitter.onCompletion(() -> emitters.remove(email));
        emitter.onTimeout(() -> emitters.remove(email));
        emitter.onError((e) -> emitters.remove(email));

        // 초기 연결 성공 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to Notification Service"));
        } catch (IOException e) {
            emitters.remove(email);
        }

        return emitter;
    }

    public void send(String receiverEmail, String name, Object data) {
        SseEmitter emitter = emitters.get(receiverEmail);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(name)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(receiverEmail);
            }
        }
    }

    public void sendToAllMembers(Long gatheringId, String name, Object data) {
        // Fetch approved members and host email from repository
        gatheringMemberRepository.findByGatheringId(gatheringId).stream()
                .filter(m -> m.getStatus() == com.example.demo.domain.MemberStatus.APPROVED)
                .forEach(m -> send(m.getUser().getEmail(), name, data));
    }
}
