package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stamps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class Stamp extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 모임에서 받은 스탬프의 대상 모임. 코스 완주 스탬프면 null. */
    private Long gatheringId;

    /** 코스 완주 스탬프의 대상 코스. 모임 스탬프면 null. */
    private Long itineraryId;

    /**
     * 이 스탬프가 가리키는 회차 날짜.
     *
     * 정기편이면 해당 회차의 날짜, 일회성 모임이면 모임 시작일이다.
     * 회차를 행으로 쌓지 않고 스탬프에 새기는 쪽을 택했기 때문에,
     * "몇 회차에 나왔는가" 는 이 값으로만 알 수 있다.
     */
    private java.time.LocalDate occurrenceDate;

    @Column(nullable = false)
    private String title;

    private String stampImageUrl;

    @Builder.Default
    private LocalDateTime completedAt = LocalDateTime.now();
}
