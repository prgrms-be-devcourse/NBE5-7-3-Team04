package me.performancereservation.global.security.oauth.service;

import me.performancereservation.domain.auth.entity.Auth;
import me.performancereservation.domain.auth.service.AuthService;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private ClientRegistration clientRegistration;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private User testUser;
    private Auth testAuth;
    private Map<String, Object> attributes;
    private static final String PROVIDER = "google";
    private static final String OAUTH_ID = "123456789";
    private static final String EMAIL = "test@example.com";
    private static final String NAME = "테스트유저";

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        testUser = User.builder()
                .id(1L)
                .email(EMAIL)
                .name(NAME)
                .role(Role.USER)
                .build();

        // 테스트용 Auth 객체 생성
        testAuth = Auth.builder()
                .id(1L)
                .userId(testUser.getId())
                .provider(PROVIDER)
                .oauthId(OAUTH_ID)
                .build();

        // 테스트용 OAuth2User attributes 생성
        attributes = new HashMap<>();
        attributes.put("sub", OAUTH_ID);
        attributes.put("email", EMAIL);
        attributes.put("name", NAME);

        // OAuth2UserRequest Mock 설정
        given(userRequest.getClientRegistration()).willReturn(clientRegistration);
        given(clientRegistration.getRegistrationId()).willReturn(PROVIDER);
        given(oAuth2User.getAttributes()).willReturn(attributes);
    }

    @Test
    @DisplayName("기존 회원 로그인: 소셜 계정이 이미 존재하는 경우")
    void loadUser_existingUser_success() {
        // given
        given(authService.getUserByProviderAndOauthId(PROVIDER, OAUTH_ID))
                .willReturn(testAuth);
        given(userService.getUserById(testUser.getId()))
                .willReturn(testUser);

        // when
        CustomOAuth2User result = (CustomOAuth2User) customOAuth2UserService.loadUserForTest(userRequest, oAuth2User);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getAttributes().get("exist")).isEqualTo(true);
    }

    @Test
    @DisplayName("신규 회원 가입: 소셜 계정이 없는 경우")
    void loadUser_newUser_success() {
        // given
        given(authService.getUserByProviderAndOauthId(PROVIDER, OAUTH_ID))
                .willThrow(ErrorCode.USER_NOT_FOUND.serviceException());
        given(userService.registerUser(EMAIL, NAME, null, Role.USER))
                .willReturn(testUser);
        given(authService.registerAuth(testUser.getId(), PROVIDER, OAUTH_ID))
                .willReturn(testAuth);
        given(userService.getUserById(testUser.getId()))
                .willReturn(testUser);

        // when
        CustomOAuth2User result = (CustomOAuth2User) customOAuth2UserService.loadUserForTest(userRequest, oAuth2User);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getAttributes().get("exist")).isEqualTo(false);
        
        // verify
        verify(userService).registerUser(EMAIL, NAME, null, Role.USER);
        verify(authService).registerAuth(testUser.getId(), PROVIDER, OAUTH_ID);
    }
}