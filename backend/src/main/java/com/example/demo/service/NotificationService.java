package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@lombok.extern.slf4j.Slf4j
public class NotificationService {

    // 사용자 이메일을 키로 하여 SseEmitter 관리
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final com.example.demo.repository.GatheringMemberRepository gatheringMemberRepository;
    private final com.example.demo.repository.GatheringRepository gatheringRepository;

    public NotificationService(com.example.demo.repository.GatheringMemberRepository gatheringMemberRepository,
                               com.example.demo.repository.GatheringRepository gatheringRepository) {
        this.gatheringMemberRepository = gatheringMemberRepository;
        this.gatheringRepository = gatheringRepository;
    }

    public SseEmitter subscribe(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "이메일 정보가 올바르지 않습니다.");
        }
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60); // 1시간 타임아웃
        emitters.put(email, emitter);

        // 연결 종료 처리.
        // 반드시 "이 emitter 일 때만" 제거해야 한다. 사용자가 새로고침하면 새 emitter 가 맵을 차지하는데,
        // 뒤늦게 죽은 이전 emitter 의 콜백이 키만 보고 지우면 살아있는 새 구독이 끊긴다.
        emitter.onCompletion(() -> removeEmitter(email, emitter));
        emitter.onTimeout(() -> removeEmitter(email, emitter));
        emitter.onError((e) -> removeEmitter(email, emitter));

        // 초기 연결 성공 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to Notification Service"));
        } catch (Exception e) {
            removeEmitter(email, emitter);
        }

        return emitter;
    }

    /**
     * 알림 전송은 best-effort 다. 어떤 이유로든 실패해도 호출자(채팅 브로드캐스트 등)의
     * 본래 동작을 깨뜨려서는 안 되므로 모든 예외를 여기서 삼킨다.
     *
     * 특히 이미 완료된 emitter 는 IOException 이 아니라 IllegalStateException 을 던지는데,
     * 이게 밖으로 새면 채팅 메시지가 저장만 되고 구독자에게 브로드캐스트되지 않는다.
     */
    public void send(String receiverEmail, String name, Object data) {
        if (receiverEmail == null || receiverEmail.trim().isEmpty()) {
            return;
        }
        SseEmitter emitter = emitters.get(receiverEmail);
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name(name != null ? name : "notification")
                    .data(data != null ? data : ""));
        } catch (Exception e) {
            log.debug("SSE 전송 실패, 구독 정리: {}", receiverEmail, e);
            removeEmitter(receiverEmail, emitter);
        }
    }

    /** 맵에 들어있는 emitter 가 인자와 동일할 때만 제거한다. */
    void removeEmitter(String email, SseEmitter emitter) {
        emitters.remove(email, emitter);
    }

    public void sendToAllMembers(Long gatheringId, String name, Object data) {
        if (gatheringId == null) {
            return;
        }
        // 수신자는 이메일만 프로젝션으로 읽는다.
        // 엔티티를 들고 오면 LAZY 프록시(gathering/user)를 트랜잭션 밖에서 건드리게 되어
        // LazyInitializationException 이 발생하고, 이 예외가 호출자(채팅 브로드캐스트)까지
        // 전파되면 메시지가 저장만 되고 구독자에게 전달되지 않는다.
        try {
            java.util.Set<String> recipientEmails = new java.util.HashSet<>();
            gatheringRepository.findHostEmailById(gatheringId).ifPresent(recipientEmails::add);
            recipientEmails.addAll(gatheringMemberRepository.findApprovedMemberEmails(gatheringId));

            recipientEmails.forEach(email -> send(email, name, data));
        } catch (Exception e) {
            // 알림은 best-effort 다. 어떤 실패도 본래 동작을 막아서는 안 된다.
            log.warn("모임 알림 전송 실패: gatheringId={}", gatheringId, e);
        }
    }
}
