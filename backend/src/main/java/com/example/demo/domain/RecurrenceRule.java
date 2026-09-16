package com.example.demo.domain;

/**
 * 모임 반복 규칙.
 *
 * 항공 은유를 그대로 쓰면 NONE 은 전세기(Charter), WEEKLY 는 정기편(Scheduled)에 해당한다.
 * 정기편은 회차마다 새 모임을 만들지 않고 하나의 모임으로 운영한다.
 * 크루 승인·채팅방·미션이 모임 단위로 유지되므로 매주 다시 모을 필요가 없다.
 */
public enum RecurrenceRule {
    /** 일회성 모임 */
    NONE,
    /** 매주 같은 요일에 반복 */
    WEEKLY
}
