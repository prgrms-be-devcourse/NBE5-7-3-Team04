package me.performancereservation.api

import me.performancereservation.api.docs.AuthApiDocs
import me.performancereservation.domain.user.enums.Role
import me.performancereservation.domain.user.service.UserService
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.security.jwt.JwtTokenProvider
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import me.performancereservation.global.storage.redis.RefreshTokenService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/auth")
class AuthController(
    private val userService: UserService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService
) : AuthApiDocs {

    //리프레쉬 토큰으로 엑세스 토큰 재발급 (리프레쉬가 유효할 때)
    @PostMapping("/reissue")
    override fun reissue(@RequestParam refreshToken: String): ResponseEntity<String> { //리프레쉬 토큰에서 uerId 추출
        jwtTokenProvider.validateToken(refreshToken, ErrorCode.INVALID_REFRESH_TOKEN)

        val userId = jwtTokenProvider.getUserId(refreshToken)

        val savedToken = refreshTokenService.getRefreshToken(userId)

        if (savedToken == null || savedToken != refreshToken) {
            throw ErrorCode.INVALID_REFRESH_TOKEN.domainException("리프레쉬 토큰이 유효하지 않습니다.")
        }

        val user = userService.getUserById(userId)
        val newAccessToken = jwtTokenProvider.createAccessToken(user)
        return ResponseEntity.ok(newAccessToken)
    }

    @PostMapping("/logout")
    override fun logout(@RequestParam userId: Long): ResponseEntity<Void> {
        refreshTokenService.deleteRefreshToken(userId)
        return ResponseEntity.noContent().build()
    }

    //이메일로 회원가입 + 토큰 발급을 테스트
    @PostMapping("/signup-test")
    override fun testSignUp(
        @RequestParam email: String, @RequestParam name: String
    ): ResponseEntity<String> {
        val user = userService.createTestUserAndToken(email, name, null.toString(), Role.USER)
        val jwt = jwtTokenProvider.createAccessToken(user)

        return ResponseEntity.ok(jwt)
    }

    //가입된 유저의 테스트용 jwt 발급
    @GetMapping("/token-test")
    override fun getTestToken(@AuthenticationPrincipal principal: CustomOAuth2User): ResponseEntity<String> {
        val user = principal.user
        val jwt = jwtTokenProvider.createAccessToken(user)
        return ResponseEntity.ok(jwt)
    }
}
