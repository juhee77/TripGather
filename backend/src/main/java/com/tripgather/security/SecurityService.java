package com.tripgather.security;

import com.tripgather.domain.User;
import com.tripgather.exception.CustomException;
import com.tripgather.exception.ErrorCode;
import com.tripgather.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public boolean isAnonymous() {
        String email = getCurrentUserEmail();
        return email == null || email.isEmpty() || email.equals("anonymousUser");
    }
}
