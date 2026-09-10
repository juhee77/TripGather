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
