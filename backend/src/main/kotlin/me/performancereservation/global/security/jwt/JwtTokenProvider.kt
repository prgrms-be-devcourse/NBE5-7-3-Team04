package me.performancereservation.global.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import lombok.extern.slf4j.Slf4j
import me.performancereservation.domain.user.entitiy.User
import me.performancereservation.domain.user.service.UserService
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Slf4j
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-expiration}") private val accessExpiration: Long,
    @Value("\${jwt.refresh-expiration}") val refreshExpiration: Long,
    private val userService: UserService
) {
    //엑세스 토큰과 리프레쉬 토큰이 존재한다.
    //지금은 엑세스 토큰만 우선 구현
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    //인증된 User 정보로 JWT 토큰을 발급
    fun createAccessToken(user: User): String {
        val now = Date()
        val expire = Date(now.time + accessExpiration)

        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("name", user.name)
            .claim("phone_number", user.phoneNumber)
            .claim("email", user.email)
            .claim("role", "ROLE_" + user.role.name) //권한 부여 시 앞에 ROLE 붙어야함
            .setIssuedAt(now)
            .setExpiration(expire)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    //리프레쉬 토큰 발급
    fun createRefreshToken(user: User): String {
        val now = Date()
        val expire = Date(now.time + refreshExpiration)

        return Jwts.builder()
            .setSubject(user.id.toString())
            .setIssuedAt(now)
            .setExpiration(expire)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    //JWT 토큰에서 인증 객체를 추출. SecurityContextHolder 에 담을 객체!!
    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val userId = claims.subject.toLong()

        // JWT 기반 인증 시, DB에서 유저 조회하여 CustomOAuth2User 생성
        val user = userService.getUserById(userId)
        val principal = CustomOAuth2User(user, emptyMap())

        //UserDetails 대신 직접 Principal 정보 구성
        return UsernamePasswordAuthenticationToken(
            principal,
            null,  // Credentials(비밀번호 없음)
            principal.authorities
        )
    }

    //JWT 토큰에서 userId 추출
    fun getUserId(token: String): Long =
        parseClaims(token).subject.toLong()

    // 엑세스 토큰의 유효성 검증
    //유효성 검증을 access,refresh 모두 하나의 검사로 처리하고 싶어서 두개를 만들기 보다는 하나를 변경해보았습니다.
    fun validateToken(token: String?, errorCode: ErrorCode): Claims {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        } catch (e: JwtException) {
            throw errorCode.serviceException()
        } catch (e: IllegalArgumentException) {
            throw errorCode.serviceException()
        }
    }

    // http 헤더로부터 bearer 토큰 분리
    fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }

    //JWT 토큰에서 Claims(내부 정보) 추출. 만료된 토큰도 Claims는 추출 가능
    private fun parseClaims(token: String): Claims {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        } catch (e: ExpiredJwtException) {
            e.claims
        }
    }
}
