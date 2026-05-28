package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"joinedGatherings", "password"})
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"joinedGatherings", "password"})
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private boolean isRead;

    @Column(updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    public static DirectMessage create(User sender, User receiver, String content) {
        return DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();
    }
}
