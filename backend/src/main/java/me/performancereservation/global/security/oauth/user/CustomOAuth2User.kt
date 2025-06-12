package me.performancereservation.global.security.oauth.user

import me.performancereservation.domain.user.entitiy.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.List

//인증된 사용자 정보를 Spring Security의 Principal로 감싸서 반환
//User 엔티티 전체를 노출하지 않고 필요한 정보만 노출
class CustomOAuth2User (
    private val user: User,
    private val attributes: Map<String, Any>
) : OAuth2User {

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority?> =
        listOf(SimpleGrantedAuthority("ROLE_" + user.role.name))

    override fun getName(): String = user.id?.toString() ?: ""

    fun getUser(): User = user

}
