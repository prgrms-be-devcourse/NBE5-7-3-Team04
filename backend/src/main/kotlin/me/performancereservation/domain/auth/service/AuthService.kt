package me.performancereservation.domain.auth.service

import me.performancereservation.domain.auth.entity.Auth
import me.performancereservation.domain.auth.repository.AuthRepository
import me.performancereservation.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authRepository: AuthRepository
) {
    fun registerAuth(userId: Long, provider: String, oauthId: String): Auth {
        if (authRepository.findByProviderAndOauthId(provider, oauthId) != null) {
            throw ErrorCode.DUPLICATE_AUTH_SOCIAL.serviceException() //이미 그 소셜 로그인으로 가입한 유저
        }
        val auth = Auth(
            userId = userId,
            provider = provider,
            oauthId = oauthId
        )
        return authRepository.save(auth)
    }

    fun getUserByProviderAndOauthId(provider: String, oauthId: String): Auth {
        return authRepository.findByProviderAndOauthId(provider, oauthId)
            ?: throw ErrorCode.USER_NOT_FOUND.serviceException()
    }
}