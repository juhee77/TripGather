package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 호스트가 모임의 크루에게 내는 미션.
 *
 * 여정의 RoutePoint 체크인이 "어디를 갔는가"를 다룬다면, 미션은 "거기서 무엇을 했는가"를 다룬다.
 * 그래서 Itinerary가 아니라 Gathering에 붙는다.
 *
 * Gathering 쪽에 역방향 컬렉션을 두지 않는다. 채팅 인증 경로에서 지연 로딩으로 두 번 데인 적이 있어
 * 미션은 처음부터 단방향으로만 참조한다.
 */
@Entity
@Table(name = "gathering_mission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@org.hibernate.annotations.SQLRestriction("deleted = false")
public class GatheringMission extends BaseEntity {

    /** 포인트 보상의 기본값. 경로 체크인(20 PTS)보다 높게 잡아 미션을 더 값지게 만든다. */
    public static final int DEFAULT_REWARD_POINTS = 50;

    /** 호스트가 걸 수 있는 포인트 보상 상한. 무제한이면 포인트 경제가 망가진다. */
    public static final int MAX_REWARD_POINTS = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Gathering gathering;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 완료를 인정받았을 때 지급할 포인트. 스탬프는 별개로 항상 1개 지급된다. */
    @Builder.Default
    private int rewardPoints = DEFAULT_REWARD_POINTS;

    /**
     * 인증 사진을 반드시 첨부해야 하는 미션인지 여부.
     * 필드명을 requiresPhoto 로 둔 것은 의도적이다. boolean 필드를 isXxx 로 지으면
     * Jackson 이 is 를 떼고 프로퍼티 이름을 xxx 로 바꿔버려 프론트와 조용히 어긋난다.
     */
    @Builder.Default
    private boolean requiresPhoto = false;

    public static GatheringMission of(Gathering gathering, String title, String description,
                                      int rewardPoints, boolean requiresPhoto) {
        return GatheringMission.builder()
                .gathering(gathering)
                .title(title)
                .description(description)
                .rewardPoints(rewardPoints)
                .requiresPhoto(requiresPhoto)
                .build();
    }
}
