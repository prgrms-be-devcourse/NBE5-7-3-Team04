package me.performancereservation.global.security.admin.handler

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.exception.ErrorResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    // 로그인을 했지만 권한이 ADMIN이 아닌 사용자가 접근한 경우의 예외를 처리하는 핸들러
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        //시큐리티의 예외는 예외 핸들러로 공통 반환이 불가능해 여기서 셋팅해서 반환해준다
        val errorCode = ErrorCode.UNAUTHORIZED_ADMIN
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