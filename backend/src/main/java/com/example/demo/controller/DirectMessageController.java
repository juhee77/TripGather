package com.example.demo.controller;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.DMResponse;
import com.example.demo.usecase.DirectMessageUseCase;
import com.example.demo.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dm")
public class DirectMessageController {

    private final DirectMessageUseCase dmService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @MessageMapping("/dm/send")
    public void sendDM(DMRequest request) {
        DirectMessage saved = dmService.sendDM(request.getSenderEmail(), request.getReceiverEmail(), request.getContent());
        
        DMResponse response = DMResponse.from(saved);

        // 발신자와 수신자 모두에게 메시지 전송 (실시간 반영)
        messagingTemplate.convertAndSend("/topic/dm/" + saved.getReceiver().getEmail(), response);
        messagingTemplate.convertAndSend("/topic/dm/" + saved.getSender().getEmail(), response);

        // 실시간 알림 전송 (SSE)
        notificationService.send(saved.getReceiver().getEmail(), "dm-received", response);
    }

    @GetMapping("/history/{otherUserEmail}")
    public List<DMResponse> getChatHistory(@PathVariable String otherUserEmail, Principal principal) {
        String myEmail = principal.getName();
        return dmService.getChatHistory(myEmail, otherUserEmail).stream()
                .map(DMResponse::from)
                .toList();
    }

    @PutMapping("/read/{otherUserEmail}")
    public void markAsRead(@PathVariable String otherUserEmail, Principal principal) {
        String myEmail = principal.getName();
        dmService.markMessagesAsRead(myEmail, otherUserEmail);
        
        // 상대방(메시지 발신자)에게 내가 읽었음을 알림
        messagingTemplate.convertAndSend("/topic/dm/read/" + otherUserEmail, new ReadNotification(myEmail));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadNotification {
        private String readerEmail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DMRequest {
        private String senderEmail;
        private String receiverEmail;
        private String content;
    }
}
