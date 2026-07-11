package com.example.demo.service;

import com.example.demo.domain.PointTransaction;
import com.example.demo.domain.Stamp;
import com.example.demo.domain.User;
import com.example.demo.repository.PointTransactionRepository;
import com.example.demo.repository.StampRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final StampRepository stampRepository;

    @Transactional
    public void addPoints(Long userId, int amount, int stampsToAdd, String description) {
        addPoints(userId, amount, stampsToAdd, description, null, null);
    }

    @Transactional
    public void addPoints(Long userId, int amount, int stampsToAdd, String description, Long gatheringId, String stampImageUrl) {
        User user = userRepository.findByIdWithPessimisticLock(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getPoints() + amount < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "잔액이 부족합니다.");
        }
        user.setPoints(user.getPoints() + amount);
        if (stampsToAdd > 0) {
            user.setStampsCount(user.getStampsCount() + stampsToAdd);

            Stamp stamp = Stamp.builder()
                    .user(user)
                    .gatheringId(gatheringId)
                    .title(description)
                    .stampImageUrl(stampImageUrl)
                    .build();
            stampRepository.save(stamp);
        }

        PointTransaction tx = PointTransaction.of(user, amount, description);

        pointTransactionRepository.save(tx);
    }
}
