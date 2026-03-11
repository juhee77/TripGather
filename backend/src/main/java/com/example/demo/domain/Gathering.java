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
public class Gathering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private String location;

    private Double lat;

    private Double lng;

    private String category;

    private String dates;

    private int currentJoining;

    private int maxJoining;

    @Column(columnDefinition = "TEXT")
    private String bgImageUrl;

    @OneToMany(mappedBy = "gathering", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Comment> comments = new java.util.ArrayList<>();

    @Transient
    private int commentCount;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PostLoad
    protected void onPostLoad() {
        if (comments != null) {
            this.commentCount = comments.size();
        }
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
