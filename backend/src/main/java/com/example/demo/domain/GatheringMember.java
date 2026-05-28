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
public class GatheringMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"members", "comments"})
    private Gathering gathering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"joinedGatherings", "password"})
    private User user;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }

    public static GatheringMember ofApprovedHost(Gathering gathering, User host) {
        return GatheringMember.builder()
                .gathering(gathering)
                .user(host)
                .status(MemberStatus.APPROVED)
                .build();
    }
}
