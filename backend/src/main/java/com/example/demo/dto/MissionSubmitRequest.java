package com.example.demo.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/** 크루가 미션 인증을 올릴 때 보내는 본문. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionSubmitRequest {
    @Size(max = 500, message = "사진 주소는 500자 이내여야 합니다.")
    private String photoUrl;
    @Size(max = 500, message = "메모는 500자 이내여야 합니다.")
    private String memo;
}
