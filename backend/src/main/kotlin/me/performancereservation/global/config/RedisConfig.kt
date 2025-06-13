package me.performancereservation.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,

    @Value("\${spring.data.redis.port}")
    private val port: Int,

    @Value("\${spring.data.redis.password}")
    private val password: String
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(host, port)
        config.setPassword(password)
        return LettuceConnectionFactory(config)
    }

    @Bean //토큰 값만 받아올 예정이기 때문에 단순 스트링으로 받아오는 방식도 괜찮아 보입니다!
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.setConnectionFactory(redisConnectionFactory())
        redisTemplate.setKeySerializer(StringRedisSerializer())

        val mapper = ObjectMapper()
        mapper.registerModule(JavaTimeModule())
        val valueSerializer = Jackson2JsonRedisSerializer(mapper, Any::class.java)
        redisTemplate.setValueSerializer(valueSerializer)

        return redisTemplate
    }
}