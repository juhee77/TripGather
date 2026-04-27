package com.example.demo.service;

import com.example.demo.domain.PointTransaction;
import com.example.demo.domain.User;
import com.example.demo.repository.PointTransactionRepository;
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

    @Transactional
    public void addPoints(Long userId, int amount, int stampsToAdd, String description) {
        User user = userRepository.findByIdWithPessimisticLock(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getPoints() + amount < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "잔액이 부족합니다.");
        }
        user.setPoints(user.getPoints() + amount);
        if (stampsToAdd > 0) {
            user.setStampsCount(user.getStampsCount() + stampsToAdd);
        }

        PointTransaction tx = PointTransaction.builder()
                .user(user)
                .amount(amount)
                .transactionType(amount >= 0 ? "EARN" : "USE")
                .description(description)
                .build();

        pointTransactionRepository.save(tx);
    }
}
