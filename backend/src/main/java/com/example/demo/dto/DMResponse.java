package com.example.demo.dto;

import com.example.demo.domain.DirectMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DMResponse {
    private Long id;
    private String content;
    private String senderName;
    private String senderEmail;
    private String receiverEmail;
    private String sentAt;
    private boolean isRead;

    public static DMResponse from(DirectMessage dm) {
        if (dm == null) return null;
        return DMResponse.builder()
                .id(dm.getId())
                .content(dm.getContent())
                .senderName(dm.getSender().getName())
                .senderEmail(dm.getSender().getEmail())
                .receiverEmail(dm.getReceiver().getEmail())
                .sentAt(dm.getSentAt().toString())
                .isRead(dm.isRead())
                .build();
    }
}
