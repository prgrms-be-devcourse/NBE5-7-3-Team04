package me.performancereservation.global.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.performancereservation.global.exception.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (isExcludedUrl(request)) {
            filterChain.doFilter(request, response)
            return
        }

        val accessToken = jwtTokenProvider.resolveToken(request) //Authorization 헤더에서 Bearer 토큰 추출

        //토큰이 유효하면 SecurityContext에 인증 정보 저장
        if (accessToken != null) {
            val claims = jwtTokenProvider.validateToken(accessToken, ErrorCode.INVALID_ACCESS_TOKEN)
            val auth = jwtTokenProvider.getAuthentication(accessToken)
            SecurityContextHolder.getContext().authentication = auth
        }
        //다음 필터로 요청 전달
        filterChain.doFilter(request, response)
    }

    private fun isExcludedUrl(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri == "/api/v1/auth/reissue" ||
                uri == "/api/v1/auth/login" ||
                uri == "/api/v1/auth/logout"
    }
}
