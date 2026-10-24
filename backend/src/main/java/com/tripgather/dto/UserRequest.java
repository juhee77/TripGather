package com.tripgather.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    private String name;
    private String profileImageUrl;
    private String bio;

    public com.tripgather.domain.User toEntity() {
        return com.tripgather.domain.User.builder()
                .email(this.email)
                .name(this.name)
                .profileImageUrl(this.profileImageUrl)
                .bio(this.bio)
                .build();
    }
}
