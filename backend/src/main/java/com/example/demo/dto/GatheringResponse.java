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
    private String dates;
    private int currentJoining;
    private int maxJoining;
    private String bgImageUrl;
    private UserResponse host;
    private List<GatheringMemberResponse> members;
    private LocalDateTime createdAt;

    public static GatheringResponse from(Gathering gathering) {
        if (gathering == null) return null;
        return GatheringResponse.builder()
                .id(gathering.getId())
                .title(gathering.getTitle())
                .location(gathering.getLocation())
                .lat(gathering.getLat())
                .lng(gathering.getLng())
                .category(gathering.getCategory())
                .dates(gathering.getDates())
                .currentJoining(gathering.getCurrentJoining())
                .maxJoining(gathering.getMaxJoining())
                .bgImageUrl(gathering.getBgImageUrl())
                .host(UserResponse.from(gathering.getHost()))
                .members(gathering.getMembers() != null ? 
                        gathering.getMembers().stream()
                                .map(GatheringMemberResponse::from)
                                .collect(Collectors.toList()) : null)
                .createdAt(gathering.getCreatedAt())
                .build();
    }
}
