package com.example.demo.dto;

import com.example.demo.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String profileImageUrl;
    private String role;
    private String bio;
    /** 본인 조회에만 담긴다. 기본형이면 값을 넣지 않아도 0 이 실려 나가므로 래퍼 타입을 쓴다. */
    private Integer points;
    private Integer stampsCount;

    /**
     * 본인에게 돌려주는 전체 프로필. 이메일과 권한이 포함된다.
     *
     * 남에게 보여주는 자리에는 쓰지 않는다. publicView 를 쓸 것.
     */
    public static UserResponse from(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .bio(user.getBio())
                .points(user.getPoints())
                .stampsCount(user.getStampsCount())
                .build();
    }

    /**
     * 남에게 보여줄 수 있는 범위만 담은 프로필.
     *
     * 이메일은 개인정보이고, 권한(role)은 누가 관리자인지 알려주므로 뺀다.
     * 보유 포인트도 본인 외에는 알 필요가 없다.
     * 호스트·크루 표시처럼 제3자가 보는 자리에는 이쪽을 쓴다.
     */
    public static UserResponse publicView(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .stampsCount(user.getStampsCount())
                .build();
    }
}
