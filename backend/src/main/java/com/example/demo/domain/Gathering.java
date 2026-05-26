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

}
