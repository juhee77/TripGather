package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.usecase.UserUseCase;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserUseCase {

    private final UserRepository userRepository;
    private final ProfanityFilterService profanityFilterService;

    @Transactional(readOnly = true)
    public User getById(Long id) {
        if (id == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "유저 ID가 올바르지 않습니다.");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 현재 로그인한 유저 반환.
     * SecurityContextHolder에서 인증 정보를 가져옵니다.
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }
        
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + email));
    }

    @Transactional(readOnly = true)
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(Long id, User update) {
        if (id == null || update == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "유저 ID 또는 프로필 정보가 올바르지 않습니다.");
        }
        User user = getById(id);
        if (update.getName() != null) {
            String trimmedName = update.getName().trim();
            if (trimmedName.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "닉네임은 공백일 수 없습니다.");
            }
            if (trimmedName.length() > 20) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "닉네임은 최대 20자까지 설정 가능합니다.");
            }
            profanityFilterService.validateText(trimmedName);
            user.setName(trimmedName);
        }
        if (update.getBio() != null) {
            profanityFilterService.validateText(update.getBio());
            user.setBio(update.getBio());
        }
        if (update.getProfileImageUrl() != null) {
            user.setProfileImageUrl(update.getProfileImageUrl());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User createUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이메일은 필수 입력값입니다.");
        }
        if (user.getName() != null) {
            if (user.getName().trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "닉네임은 공백일 수 없습니다.");
            }
            profanityFilterService.validateText(user.getName());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = getById(id);
        user.setName("탈퇴한 회원");
        user.setBio("탈퇴한 회원입니다.");
        user.setProfileImageUrl("/default-profile.png");
        userRepository.save(user);
    }
}
