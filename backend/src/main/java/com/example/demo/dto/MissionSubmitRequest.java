package com.example.demo.dto;

import lombok.*;

/** 크루가 미션 인증을 올릴 때 보내는 본문. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionSubmitRequest {
    private String photoUrl;
    private String memo;
}
