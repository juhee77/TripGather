package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();
    }

    @Test
    @DisplayName("ID로 유저 조회 성공")
    void getById_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // when
        User result = userService.getById(1L);

        // then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 유저 조회 시 예외 발생")
    void getById_NotFound_ThrowsException() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("현재 로그인한 유저 조회 성공")
    void getCurrentUser_Success() {
        // given
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getName()).willReturn("test@example.com");
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));

        // when
        User result = userService.getCurrentUser();

        // then
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("프로필 업데이트 성공")
    void updateProfile_Success() {
        // given
        User updateInfo = User.builder()
                .name("Updated Name")
                .bio("New Bio")
                .build();
        
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User updated = userService.updateProfile(1L, updateInfo);

        // then
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getBio()).isEqualTo("New Bio");
        verify(userRepository).save(any(User.class));
    }
}
