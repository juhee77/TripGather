package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private final String KEY = "test-user@example.com";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    void whenLoginFailsFiveTimes_thenIsBlockedIsTrue() {
        assertFalse(loginAttemptService.isBlocked(KEY));

        for (int i = 0; i < 5; i++) {
            loginAttemptService.loginFailed(KEY);
        }

        assertTrue(loginAttemptService.isBlocked(KEY));
    }

    @Test
    void whenLoginSucceeds_thenAttemptsAreReset() {
        loginAttemptService.loginFailed(KEY);
        loginAttemptService.loginFailed(KEY);
        
        loginAttemptService.loginSucceeded(KEY);
        
        // Fail 4 more times (total would be 6 if not reset, but here it should be 4)
        for (int i = 0; i < 4; i++) {
            loginAttemptService.loginFailed(KEY);
        }

        assertFalse(loginAttemptService.isBlocked(KEY));
        
        loginAttemptService.loginFailed(KEY); // 5th time
        assertTrue(loginAttemptService.isBlocked(KEY));
    }
}
