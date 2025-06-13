package me.performancereservation.domain.admin.service;

import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AdminService adminService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증된 사용자의 인증 확인 성공")
    void checkAuthentication_Success() {
        // given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin@test.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when & then
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            assertDoesNotThrow(() -> adminService.checkAuthentication());
        }
    }

    @Test
    @DisplayName("인증이 null인 경우 예외 발생")
    void checkAuthentication_AuthenticationNull() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            AppException exception = assertThrows(AppException.class, () ->
                    adminService.checkAuthentication()
            );

            assertEquals(ErrorCode.ADMIN_AUTHENTICATION_REQUIRED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 경우 예외 발생")
    void checkAuthentication_NotAuthenticated() {
        // given
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when & then
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            AppException exception = assertThrows(AppException.class, () ->
                    adminService.checkAuthentication()
            );

            assertEquals(ErrorCode.ADMIN_AUTHENTICATION_REQUIRED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("익명 사용자의 경우 예외 발생")
    void checkAuthentication_AnonymousUser() {
        // given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // when & then
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            AppException exception = assertThrows(AppException.class, () ->
                    adminService.checkAuthentication()
            );

            assertEquals(ErrorCode.ADMIN_AUTHENTICATION_REQUIRED, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("사용자 이름 조회 성공")
    void getUserName_Success() {
        // given
        Long userId = 1L;
        String expectedName = "테스트 사용자";

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .name(expectedName)
                .phoneNumber("010-1234-5678")
                .role(Role.USER)
                .build();

        // when
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        String result = adminService.getUserName(userId);

        // then
        assertEquals(expectedName, result);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 이름 조회 시도")
    void getUserName_UserNotFound() {
        // given
        Long userId = 1L;

        // when
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminService.getUserName(userId)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("관리자 이름 조회 성공")
    void getUserName_AdminUser() {
        // given
        Long userId = 1L;
        String expectedName = "관리자";

        User adminUser = User.builder()
                .id(userId)
                .email("admin@test.com")
                .name(expectedName)
                .phoneNumber("010-9999-9999")
                .role(Role.ADMIN)
                .build();

        // when
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(adminUser));

        String result = adminService.getUserName(userId);

        // then
        assertEquals(expectedName, result);
        verify(userRepository).findById(userId);
    }

}