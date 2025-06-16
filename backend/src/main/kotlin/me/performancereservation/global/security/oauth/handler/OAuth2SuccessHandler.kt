package me.performancereservation.global.security.oauth.handler

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.performancereservation.global.security.jwt.JwtTokenProvider
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import me.performancereservation.global.storage.redis.RefreshTokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Configuration
class OAuth2SuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService
) : AuthenticationSuccessHandler {

    @Value("\${spring.oauth2.url.callback}")
    private lateinit var callbackUrl: String

    @Value("\${spring.oauth2.url.sign-up}")
    private lateinit var signupUrl: String

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as CustomOAuth2User

        val user = oAuth2User.user

        val accessToken = jwtTokenProvider.createAccessToken(user)
        val refreshToken = jwtTokenProvider.createRefreshToken(user)

        //리프레쉬 토큰 레디스에 저장
        user.id?.let {
            refreshTokenService.saveRefreshToken(
                it,
                refreshToken,
                jwtTokenProvider.refreshExpiration
            )
        }

        val isExist = oAuth2User.getAttribute<Boolean>("exist") ?: false

        val redirectUrl = if (isExist) { //회원이면 로그인 하고 콜백 페이지로
            UriComponentsBuilder.fromUriString(callbackUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .toUriString()
        } else { //아니면 회원가입 유도
            UriComponentsBuilder.fromUriString(signupUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .toUriString()
        }
        response.sendRedirect(redirectUrl)
    }
}
