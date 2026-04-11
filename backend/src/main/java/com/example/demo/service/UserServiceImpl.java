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

    @Transactional(readOnly = true)
    public User getById(Long id) {
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
        User user = getById(id);
        if (update.getName() != null) {
            user.setName(update.getName());
        }
        if (update.getBio() != null) {
            user.setBio(update.getBio());
        }
        if (update.getProfileImageUrl() != null) {
            user.setProfileImageUrl(update.getProfileImageUrl());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
