package me.performancereservation.global.storage.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private static final Duration TEST_EXPIRE_DURATION = Duration.ofHours(1);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenPrefix", REFRESH_TOKEN_PREFIX);
    }

    @Test
    @DisplayName("리프레시 토큰 저장: 성공")
    void saveRefreshToken_success() {
        // given
        String expectedKey = REFRESH_TOKEN_PREFIX + TEST_USER_ID;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        refreshTokenService.saveRefreshToken(TEST_USER_ID, TEST_REFRESH_TOKEN, TEST_EXPIRE_DURATION.toSeconds());

        // then
        verify(valueOperations).set(eq(expectedKey), eq(TEST_REFRESH_TOKEN), eq(TEST_EXPIRE_DURATION));
    }

    @Test
    @DisplayName("리프레시 토큰 조회: 성공")
    void getRefreshToken_success() {
        // given
        String expectedKey = REFRESH_TOKEN_PREFIX + TEST_USER_ID;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

        // when
        String result = refreshTokenService.getRefreshToken(TEST_USER_ID);

        // then
        assertThat(result).isEqualTo(TEST_REFRESH_TOKEN);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("리프레시 토큰 조회: 토큰 없음")
    void getRefreshToken_notFound() {
        // given
        String expectedKey = REFRESH_TOKEN_PREFIX + TEST_USER_ID;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(expectedKey)).thenReturn(null);

        // when
        String result = refreshTokenService.getRefreshToken(TEST_USER_ID);

        // then
        assertThat(result).isNull();
        verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("리프레시 토큰 삭제: 성공")
    void deleteRefreshToken_success() {
        // given
        String expectedKey = REFRESH_TOKEN_PREFIX + TEST_USER_ID;

        // when
        refreshTokenService.deleteRefreshToken(TEST_USER_ID);

        // then
        verify(redisTemplate).delete(expectedKey);
    }
} 