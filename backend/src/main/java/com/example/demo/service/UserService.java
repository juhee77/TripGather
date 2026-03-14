package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    /** 로그인 미구현 시 기본으로 반환할 유저 ID */
    private static final long DEFAULT_CURRENT_USER_ID = 1L;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    /**
     * 현재 로그인한 유저 반환.
     * 인증 미구현 시 ID=1 유저를 기본값으로 사용.
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        return userRepository.findById(DEFAULT_CURRENT_USER_ID)
                .orElseThrow(() -> new IllegalStateException("Default user (id=1) not found. Run DataInitializer."));
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
