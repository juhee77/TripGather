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
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Gathering extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private User host;

    @Column(nullable = false)
    private String location;

    private Double lat;

    private Double lng;

    private String category;

    private java.time.LocalDate startDate;

    private java.time.LocalDate endDate;

    /** 반복 규칙. 기본은 일회성(NONE). */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RecurrenceRule recurrenceRule = RecurrenceRule.NONE;

    /** WEEKLY 일 때 반복되는 요일. NONE 이면 null. */
    @Enumerated(EnumType.STRING)
    private java.time.DayOfWeek recurrenceDayOfWeek;

    /** 반복 종료일(포함). null 이면 기한 없이 계속된다. */
    private java.time.LocalDate recurrenceUntil;

    private int currentJoining;

    private int maxJoining;

    @Column(columnDefinition = "TEXT")
    private String bgImageUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GatheringStatus status = GatheringStatus.OPEN;

    @Builder.Default
    private int likeCount = 0;

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"gathering"})
    @Builder.Default
    private java.util.List<Comment> comments = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"gathering"})
    @Builder.Default
    private java.util.List<GatheringMember> members = new java.util.ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary linkedItinerary;

    @Transient
    private int commentCount;

    @Transient
    private int memberCount;



    @Builder.Default
    private boolean isGalleryPublic = false;

    @Builder.Default
    private boolean isChatPublic = false;

    @Builder.Default
    private boolean isCommentPublic = true;

    @PostLoad
    protected void onPostLoad() {
        if (comments != null) {
            this.commentCount = comments.size();
        }
        if (members != null) {
            this.memberCount = (int) members.stream()
                    .filter(m -> m.getStatus() == MemberStatus.APPROVED)
                    .count();
        }
    }

    /** 정기 모임인가 */
    public boolean isRecurring() {
        return recurrenceRule == RecurrenceRule.WEEKLY && recurrenceDayOfWeek != null;
    }

    /**
     * from 이후(당일 포함)로 다가오는 회차 날짜를 최대 limit 개 계산한다.
     *
     * 회차를 테이블에 미리 쌓아 두지 않고 그때그때 계산한다.
     * 미리 만들어 두면 "언제까지 만들어 둘 것인가", "규칙이 바뀌면 이미 만든 회차는
     * 어떻게 할 것인가" 같은 문제가 따라오는데, 조회 시 계산하면 그런 상태가 생기지 않는다.
     */
    public java.util.List<java.time.LocalDate> upcomingOccurrences(java.time.LocalDate from, int limit) {
        if (!isRecurring() || from == null || limit <= 0) {
            return java.util.List.of();
        }

        // 반복 시작 기준일이 미래라면 그날부터 센다.
        java.time.LocalDate cursor = (startDate != null && startDate.isAfter(from)) ? startDate : from;
        // 기준일 이후 가장 가까운 해당 요일로 맞춘다. (기준일이 그 요일이면 그대로)
        cursor = cursor.with(java.time.temporal.TemporalAdjusters.nextOrSame(recurrenceDayOfWeek));

        java.util.List<java.time.LocalDate> result = new java.util.ArrayList<>();
        while (result.size() < limit) {
            if (recurrenceUntil != null && cursor.isAfter(recurrenceUntil)) {
                break;
            }
            result.add(cursor);
            cursor = cursor.plusWeeks(1);
        }
        return result;
    }

    /** 다음 회차. 없으면 null. */
    public java.time.LocalDate nextOccurrence(java.time.LocalDate from) {
        java.util.List<java.time.LocalDate> next = upcomingOccurrences(from, 1);
        return next.isEmpty() ? null : next.get(0);
    }
}
