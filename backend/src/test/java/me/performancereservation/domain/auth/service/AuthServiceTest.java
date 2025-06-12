package me.performancereservation.domain.auth.service;

import me.performancereservation.domain.auth.entity.Auth;
import me.performancereservation.domain.auth.repository.AuthRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private AuthService authService;

    private Auth testAuth;
    private static final Long USER_ID = 1L;
    private static final String PROVIDER = "google";
    private static final String OAUTH_ID = "123456789";

    @BeforeEach
    void setUp() {
        testAuth = Auth.builder()
                .id(1L)
                .userId(USER_ID)
                .provider(PROVIDER)
                .oauthId(OAUTH_ID)
                .build();
    }

    @Test
    @DisplayName("소셜 로그인 인증 정보 등록: 성공")
    void registerAuth_success() {
        // given
        given(authRepository.findByProviderAndOauthId(PROVIDER, OAUTH_ID)).willReturn(Optional.empty());
        given(authRepository.save(any(Auth.class))).willReturn(testAuth);

        // when
        Auth result = authService.registerAuth(USER_ID, PROVIDER, OAUTH_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getProvider()).isEqualTo(PROVIDER);
        assertThat(result.getOauthId()).isEqualTo(OAUTH_ID);

        // verify
        verify(authRepository).findByProviderAndOauthId(PROVIDER, OAUTH_ID);
        verify(authRepository).save(any(Auth.class));
    }

    @Test
    @DisplayName("소셜 로그인 인증 정보 등록: 중복된 소셜 계정")
    void registerAuth_duplicateSocialAccount() {
        // given
        given(authRepository.findByProviderAndOauthId(PROVIDER, OAUTH_ID)).willReturn(Optional.of(testAuth));

        // when & then
        assertThatThrownBy(() -> authService.registerAuth(USER_ID, PROVIDER, OAUTH_ID))
                .isInstanceOf(ErrorCode.DUPLICATE_AUTH_SOCIAL.serviceException().getClass());

        // verify
        verify(authRepository).findByProviderAndOauthId(PROVIDER, OAUTH_ID);
        verify(authRepository, never()).save(any(Auth.class));
    }

    @Test
    @DisplayName("소셜 로그인 인증 정보 조회: 성공")
    void getUserByProviderAndOauthId_success() {
        // given
        given(authRepository.findByProviderAndOauthId(PROVIDER, OAUTH_ID)).willReturn(Optional.of(testAuth));

        // when
        Auth result = authService.getUserByProviderAndOauthId(PROVIDER, OAUTH_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getProvider()).isEqualTo(PROVIDER);
        assertThat(result.getOauthId()).isEqualTo(OAUTH_ID);

        // verify
        verify(authRepository).findByProviderAndOauthId(PROVIDER, OAUTH_ID);
    }

    @Test
    @DisplayName("소셜 로그인 인증 정보 조회: 존재하지 않는 계정")
    void getUserByProviderAndOauthId_notFound() {
        // given
        given(authRepository.findByProviderAndOauthId(PROVIDER, OAUTH_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.getUserByProviderAndOauthId(PROVIDER, OAUTH_ID))
                .isInstanceOf(ErrorCode.USER_NOT_FOUND.serviceException().getClass());

        // verify
        verify(authRepository).findByProviderAndOauthId(PROVIDER, OAUTH_ID);
    }
} 