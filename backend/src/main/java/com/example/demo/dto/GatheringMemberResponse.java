package com.example.demo.dto;

import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatheringMemberResponse {
    private Long id;
    private UserResponse user;
    private MemberStatus status;
    private LocalDateTime requestedAt;

    public static GatheringMemberResponse from(GatheringMember member) {
        if (member == null) return null;
        return GatheringMemberResponse.builder()
                .id(member.getId())
                .user(UserResponse.from(member.getUser()))
                .status(member.getStatus())
                .requestedAt(member.getRequestedAt())
                .build();
    }
}
