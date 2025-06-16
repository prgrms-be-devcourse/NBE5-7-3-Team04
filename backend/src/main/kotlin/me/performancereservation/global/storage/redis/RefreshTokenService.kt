package me.performancereservation.global.storage.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RefreshTokenService(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${spring.data.redis.refresh-token}")
    private val refreshTokenPrefix: String
) {

    fun saveRefreshToken(userId: Long, refreshToken: String, expireSeconds: Long) {
        val key = "$refreshTokenPrefix$userId"
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(expireSeconds))
    }

    fun getRefreshToken(userId: Long): String? {
        return redisTemplate.opsForValue().get("$refreshTokenPrefix$userId")
    }

    fun deleteRefreshToken(userId: Long) {
        redisTemplate.delete("$refreshTokenPrefix$userId")
    }
}