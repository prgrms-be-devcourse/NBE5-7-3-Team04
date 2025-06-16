package me.performancereservation.global.storage.redis

import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
@RequiredArgsConstructor
class RefreshTokenService {
    private val redisTemplate: StringRedisTemplate? = null

    @Value("\${spring.data.redis.refresh-token}")
    private val refreshTokenPrefix: String? = null

    fun saveRefreshToken(userId: Long, refreshToken: String, expireSeconds: Long) {
        val key = refreshTokenPrefix + userId
        redisTemplate!!.opsForValue()[key, refreshToken] = Duration.ofSeconds(expireSeconds)
    }

    fun getRefreshToken(userId: Long): String? {
        return redisTemplate!!.opsForValue()[refreshTokenPrefix + userId]
    }

    //리프레쉬 토큰 삭제 : 만료, 로그아웃 등
    fun deleteRefreshToken(userId: Long) {
        redisTemplate!!.delete(refreshTokenPrefix + userId)
    }
}
