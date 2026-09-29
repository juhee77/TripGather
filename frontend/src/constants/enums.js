export const MemberStatus = Object.freeze({
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
});

/** 크루가 올린 미션 인증의 심사 상태. 백엔드 MissionCompletionStatus 와 짝을 이룬다. */
export const MissionCompletionStatus = Object.freeze({
    SUBMITTED: 'SUBMITTED',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
});

/**
 * 모임 정원의 허용 범위. 백엔드 Gathering.MIN_CAPACITY / MAX_CAPACITY 와 짝을 이룬다.
 *
 * 판단의 주인은 서버(GatheringServiceImpl)다. 화면에서는 같은 값으로 미리 걸러
 * 사용자가 저장 버튼을 누른 뒤에야 거절당하지 않게 한다.
 */
export const GatheringCapacity = Object.freeze({
    MIN: 2,
    MAX: 100
});
