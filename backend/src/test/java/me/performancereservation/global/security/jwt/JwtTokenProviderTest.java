package me.performancereservation.global.security.jwt;

import io.jsonwebtoken.Claims;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    @Mock
    private UserService userService;

    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String secret = "testtesttesttesttesttesttesttesttesttesttesttesttesttest"; // 64byte 이상
    private long accessExpiration = 1000 * 60 * 10; // 10분
    private long refreshExpiration = 1000 * 60 * 60 * 24 * 7; // 7일

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, accessExpiration, refreshExpiration, userService);
        testUser = User.builder()
                .id(1L)
                .name("테스트유저")
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("createAccessToken: 유저 정보로 AccessToken이 정상 생성되고 Claims가 올바르게 들어간다")
    void createAccessToken_success() {
        // when
        String token = jwtTokenProvider.createAccessToken(testUser);
        // then
        Claims claims = jwtTokenProvider.validateToken(token, ErrorCode.INVALID_ACCESS_TOKEN);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(testUser.getId()));
        assertThat(claims.get("name")).isEqualTo(testUser.getName());
        assertThat(claims.get("email")).isEqualTo(testUser.getEmail());
        assertThat(claims.get("phone_number")).isEqualTo(testUser.getPhoneNumber());
        assertThat(claims.get("role")).isEqualTo("ROLE_" + testUser.getRole().name());
    }

    @Test
    @DisplayName("createRefreshToken: 유저 정보로 RefreshToken이 정상 생성되고 subject가 userId이다")
    void createRefreshToken_success() {
        // when
        String token = jwtTokenProvider.createRefreshToken(testUser);
        // then
        Claims claims = jwtTokenProvider.validateToken(token, ErrorCode.INVALID_ACCESS_TOKEN);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(testUser.getId()));
        // RefreshToken에는 추가 Claims가 없음
    }

    @Test
    @DisplayName("getUserId: 토큰에서 userId가 올바르게 추출된다")
    void getUserId_success() {
        // given
        String token = jwtTokenProvider.createAccessToken(testUser);
        // when
        Long userId = jwtTokenProvider.getUserId(token);
        // then
        assertThat(userId).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("validateToken: 정상 토큰은 예외 없이 Claims를 반환한다")
    void validateToken_success() {
        // given
        String token = jwtTokenProvider.createAccessToken(testUser);
        // when
        Claims claims = jwtTokenProvider.validateToken(token, ErrorCode.INVALID_ACCESS_TOKEN);
        // then
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(testUser.getId()));
    }

    @Test
    @DisplayName("validateToken: 변조된 토큰/잘못된 토큰은 예외를 던진다")
    void validateToken_invalidToken_throwsException() {
        // given
        String invalidToken = "invalid.token.value";
        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken, ErrorCode.INVALID_ACCESS_TOKEN))
                .isInstanceOf(RuntimeException.class); // 실제 서비스 Exception 타입에 맞게 수정 가능
    }

    @Test
    @DisplayName("getAuthentication: 토큰에서 Authentication 객체가 올바르게 생성된다")
    void getAuthentication_success() {
        // given
        String token = jwtTokenProvider.createAccessToken(testUser);
        given(userService.getUserById(testUser.getId())).willReturn(testUser);
        // when
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isNotNull();
        assertThat(authentication.getAuthorities()).isNotEmpty();
        assertThat(authentication.getName()).isEqualTo(String.valueOf(testUser.getName()));
    }

    @Test
    @DisplayName("resolveToken: Authorization 헤더에서 Bearer 토큰이 올바르게 추출된다")
    void resolveToken_success() {
        // given
        String bearerToken = "Bearer test.jwt.token";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        // when
        String token = jwtTokenProvider.resolveToken(request);
        // then
        assertThat(token).isEqualTo("test.jwt.token");
    }

    @Test
    @DisplayName("resolveToken: Authorization 헤더가 없거나 잘못된 경우 null을 반환한다")
    void resolveToken_invalidHeader_returnsNull() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        // when
        String token = jwtTokenProvider.resolveToken(request);
        // then
        assertThat(token).isNull();

        // given - 잘못된 prefix
        when(request.getHeader("Authorization")).thenReturn("Basic test.jwt.token");
        // when
        String token2 = jwtTokenProvider.resolveToken(request);
        // then
        assertThat(token2).isNull();
    }

    // 이후에 각 메서드별 테스트가 추가될 예정
}