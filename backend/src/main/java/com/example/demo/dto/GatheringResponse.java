package com.example.demo.dto;

import com.example.demo.domain.Gathering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringResponse {
    private Long id;
    private String title;
    private String location;
    private Double lat;
    private Double lng;
    private String category;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private int currentJoining;
    private int maxJoining;
    private String bgImageUrl;
    private UserResponse host;
    private List<GatheringMemberResponse> members;
    private int likeCount;
    private boolean likedByCurrentUser;
    @com.fasterxml.jackson.annotation.JsonProperty("isGalleryPublic")
    private boolean isGalleryPublic;
    @com.fasterxml.jackson.annotation.JsonProperty("isChatPublic")
    private boolean isChatPublic;
    @com.fasterxml.jackson.annotation.JsonProperty("isCommentPublic")
    private boolean isCommentPublic;
    private LocalDateTime createdAt;

    public static GatheringResponse from(Gathering gathering) {
        return from(gathering, false);
    }

    public static GatheringResponse from(Gathering gathering, boolean isLiked) {
        if (gathering == null) return null;
        return GatheringResponse.builder()
                .id(gathering.getId())
                .title(gathering.getTitle())
                .location(gathering.getLocation())
                .lat(gathering.getLat())
                .lng(gathering.getLng())
                .category(gathering.getCategory())
                .startDate(gathering.getStartDate())
                .endDate(gathering.getEndDate())
                .currentJoining(gathering.getCurrentJoining())
                .maxJoining(gathering.getMaxJoining())
                .bgImageUrl(gathering.getBgImageUrl())
                .host(UserResponse.from(gathering.getHost()))
                .members(gathering.getMembers() != null ? 
                        gathering.getMembers().stream()
                                .map(GatheringMemberResponse::from)
                                .collect(Collectors.toList()) : null)
                .likeCount(gathering.getLikeCount())
                .likedByCurrentUser(isLiked)
                .isGalleryPublic(gathering.isGalleryPublic())
                .isChatPublic(gathering.isChatPublic())
                .isCommentPublic(gathering.isCommentPublic())
                .createdAt(gathering.getCreatedAt())
                .build();
    }
}
