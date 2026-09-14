package com.example.demo.service;

import com.example.demo.domain.Trip;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.TripRepository;
import com.example.demo.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 여행(Trip)과 그 하위 자원(준비물/지출/후기)에 대한 소유권 검사.
 *
 * Trip 은 공유 개념이 없는 개인 자원인데, 조회 경로에는 소유자 확인이 빠져 있어
 * 로그인한 사용자라면 누구나 tripId 만 바꿔 남의 여행 데이터를 읽을 수 있었다.
 * 서비스마다 검사를 흩어두면 또 빠뜨리게 되므로 한 곳에서 처리한다.
 */
@Component
@RequiredArgsConstructor
public class TripAccessGuard {

    private final TripRepository tripRepository;
    private final SecurityService securityService;

    /** 소유자임을 확인하고 Trip 을 돌려준다. 아니면 예외. */
    @Transactional(readOnly = true)
    public Trip requireOwner(Long tripId) {
        if (tripId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행 ID가 올바르지 않습니다.");
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행을 찾을 수 없습니다."));

        if (securityService.isAnonymous()) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "본인의 여행만 조회할 수 있습니다.");
        }
        String email = securityService.getCurrentUserEmail();
        if (trip.getOwner() == null || !email.equals(trip.getOwner().getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "본인의 여행만 조회할 수 있습니다.");
        }
        return trip;
    }
}
