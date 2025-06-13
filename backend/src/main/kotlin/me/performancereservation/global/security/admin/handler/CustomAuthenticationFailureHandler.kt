package me.performancereservation.global.security.admin.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.exception.ErrorResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class CustomAuthenticationFailureHandler(
    private val objectMapper: ObjectMapper
) : AuthenticationFailureHandler {

    // 어드민의 인증정보(아이디, 비밀번호)가 틀린 경우의 예외를 처리하는 핸들러
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorCode = ErrorCode.ADMIN_NOT_FOUND
        val errorResponse = ErrorResponse.from(errorCode)

        val adminId = request.getParameter("adminId")
        val clientIp = getClientIp(request)

        log.error(exception) {
            "Admin login failed - ID: $adminId, IP: $clientIp, Reason: ${exception.message}"
        }

        // response에 예외를 셋팅해 반환해 줍니다.
        response.apply {
            status = errorCode.httpStatus.value()
            contentType = "application/json"
            characterEncoding = "UTF-8"
            writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (!xForwardedFor.isNullOrEmpty()) {
            xForwardedFor.split(",")[0].trim()
        } else {
            request.remoteAddr
        }
    }
}