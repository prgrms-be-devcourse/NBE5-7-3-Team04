package me.performancereservation.global.security.admin.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class AdminAuthSuccessHandler : AuthenticationSuccessHandler {

    // 로그인이 성공한 경우 OK를 반환하는 핸들러
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        // 로그인이 성공하면 OK 반환
        response.status = HttpStatus.OK.value()
    }
}