package com.example.demo.domain;

/** 크루가 제출한 미션 인증의 심사 상태. */
public enum MissionCompletionStatus {
    /** 크루가 인증을 올렸고 호스트 확인을 기다리는 중 */
    SUBMITTED,
    /** 호스트가 인정했고 보상(포인트 + 스탬프)이 지급된 상태 */
    APPROVED,
    /** 호스트가 반려한 상태. 크루는 다시 제출할 수 있다 */
    REJECTED
}
