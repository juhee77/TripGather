package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 준비물 항목 추가 본문.
 *
 * 항목명·카테고리 길이와 비속어는 PackingService 가 이미 검증한다.
 * 여기서는 API 모양을 드러내기 위해 타입만 명시한다(Map 으로 받으면 문서에 자유 형식으로만 나온다).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingItemRequest {
    private String name;
    private String category;
}
