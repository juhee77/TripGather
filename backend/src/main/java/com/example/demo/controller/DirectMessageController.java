package com.example.demo.controller;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DirectMessageService;
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

    private final DirectMessageService dmService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/dm/send")
    public void sendDM(DMRequest request) {
        DirectMessage saved = dmService.sendDM(request.getSenderEmail(), request.getReceiverEmail(), request.getContent());
        
        DMResponse response = new DMResponse(
                saved.getId(),
                saved.getContent(),
                saved.getSender().getName(),
                saved.getSender().getEmail(),
                saved.getReceiver().getEmail(),
                saved.getSentAt().toString()
        );

        // 발신자와 수신자 모두에게 메시지 전송 (실시간 반영)
        messagingTemplate.convertAndSend("/topic/dm/" + saved.getReceiver().getEmail(), response);
        messagingTemplate.convertAndSend("/topic/dm/" + saved.getSender().getEmail(), response);
    }

    @GetMapping("/history/{otherUserEmail}")
    public List<DMResponse> getChatHistory(@PathVariable String otherUserEmail, Principal principal) {
        String myEmail = principal.getName();
        return dmService.getChatHistory(myEmail, otherUserEmail).stream()
                .map(dm -> new DMResponse(
                        dm.getId(),
                        dm.getContent(),
                        dm.getSender().getName(),
                        dm.getSender().getEmail(),
                        dm.getReceiver().getEmail(),
                        dm.getSentAt().toString()
                )).toList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DMRequest {
        private String senderEmail;
        private String receiverEmail;
        private String content;
    }

    @Data
    @AllArgsConstructor
    public static class DMResponse {
        private Long id;
        private String content;
        private String senderName;
        private String senderEmail;
        private String receiverEmail;
        private String sentAt;
    }
}
