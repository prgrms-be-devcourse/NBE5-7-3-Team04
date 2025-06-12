package me.performancereservation.domain.auth.repository

import me.performancereservation.domain.auth.entity.Auth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AuthRepository : JpaRepository<Auth, Long?> {
    fun findByProviderAndOauthId(provider: String, oauthId: String): Auth? //소셜로그인한 유저를 조회
}
