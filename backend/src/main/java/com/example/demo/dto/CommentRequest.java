package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    private String content;

    public com.example.demo.domain.Comment toEntity() {
        return com.example.demo.domain.Comment.builder()
                .content(this.content)
                .build();
    }
}
