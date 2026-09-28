package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    @Size(max = 1000, message = "댓글은 1000자 이내여야 합니다.")
    private String content;

    public com.example.demo.domain.Comment toEntity() {
        return com.example.demo.domain.Comment.builder()
                .content(this.content)
                .build();
    }
}
