package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 크루 한 명이 미션 하나에 대해 올린 인증 기록.
 *
 * (mission_id, user_id) 조합은 유일하다. 한 사람이 같은 미션에 인증을 여러 번 쌓으면
 * 호스트가 무엇을 심사해야 하는지 알 수 없어진다. 반려당한 경우에는 기존 기록을
 * 다시 SUBMITTED 로 되돌려 재제출한다.
 */
@Entity
@Table(
        name = "mission_completion",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mission_completion_mission_user",
                columnNames = {"mission_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class MissionCompletion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private GatheringMission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    /** 인증 사진 URL. FileController 로 먼저 업로드한 뒤 그 주소를 넘긴다. */
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MissionCompletionStatus status = MissionCompletionStatus.SUBMITTED;

    /** 호스트가 승인하거나 반려한 시각. 심사 전에는 null 이다. */
    private LocalDateTime reviewedAt;

    public static MissionCompletion of(GatheringMission mission, User user, String photoUrl, String memo) {
        return MissionCompletion.builder()
                .mission(mission)
                .user(user)
                .photoUrl(photoUrl)
                .memo(memo)
                .status(MissionCompletionStatus.SUBMITTED)
                .build();
    }

    /** 반려당한 인증을 새 내용으로 갈아끼워 다시 심사대에 올린다. */
    public void resubmit(String photoUrl, String memo) {
        this.photoUrl = photoUrl;
        this.memo = memo;
        this.status = MissionCompletionStatus.SUBMITTED;
        this.reviewedAt = null;
    }

    public void approve() {
        this.status = MissionCompletionStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = MissionCompletionStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
    }
}
