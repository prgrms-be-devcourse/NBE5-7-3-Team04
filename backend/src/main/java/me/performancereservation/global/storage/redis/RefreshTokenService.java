package me.performancereservation.global.storage.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${spring.data.redis.refresh-token}")
    private String refreshTokenPrefix;

    public void saveRefreshToken(Long userId, String refreshToken, long expireSeconds) {
        String key = refreshTokenPrefix + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(expireSeconds));
    }

    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(refreshTokenPrefix + userId);
    }

    //리프레쉬 토큰 삭제 : 만료, 로그아웃 등
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(refreshTokenPrefix + userId);
    }
}
