package me.performancereservation.global.security.admin.handler

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.exception.ErrorResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    // 로그인을 하지 않고 admin 기능에 접근한 경우의 예외를 처리하는 핸들러
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val errorCode = ErrorCode.ADMIN_AUTHENTICATION_REQUIRED
        val errorResponse = ErrorResponse.from(errorCode)

        // response에 예외를 셋팅해 반환해 줍니다.
        response.apply {
            status = errorCode.httpStatus.value()
            contentType = "application/json"
            characterEncoding = "UTF-8"
            writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }
}