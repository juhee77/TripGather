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
        // This is a simplified version; in a real app, you'd fetch members and send to each if connected.
        // For now, we'll just log and send to any connected emitters that might be interested,
        // but typically SSE is 1:1. So we broadcast to all for this specific event type if needed,
        // or just rely on the fact that members will be subscribed by their email.
        emitters.forEach((email, emitter) -> send(email, name, data));
    }
}
