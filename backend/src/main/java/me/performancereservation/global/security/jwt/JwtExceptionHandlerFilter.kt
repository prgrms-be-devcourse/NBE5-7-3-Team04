package me.performancereservation.global.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.performancereservation.global.exception.AppException
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.exception.ErrorResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtExceptionHandlerFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: AppException) {
            logger.warn("[JwtExceptionHandlerFilter] AppException 발생: {}")
            setErrorResponse(response, e.errorCode)
        } catch (e: ExpiredJwtException) { //토큰 만료
            setErrorResponse(response, ErrorCode.ACCESS_TOKEN_EXPIRED)
            logger.info("[JwtExceptionHandlerFilter] error name = ExpiredJwtException")
        } catch (e: IllegalArgumentException) { // 잘못된 인수나 인수의 값이 올바르지 않은 경우
            setErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN)
            logger.info("[JwtExceptionHandlerFilter] error name = IllegalArgumentException")
        } catch (e: NullPointerException) { //토큰이 NULL인 경우(헤더 누락 등)
            setErrorResponse(response, ErrorCode.TOKEN_NOT_FOUND)
            logger.info("[JwtExceptionHandlerFilter] error name = NullPointerException")
        } catch (e: Exception) { //기타 오류 발생
            setErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN)
            logger.info("[JwtExceptionHandlerFilter] error name = UNKNOWN_Exception")
        }
    }

    private fun setErrorResponse(
        response: HttpServletResponse,
        errorCode: ErrorCode
    ) {
        val objectMapper = ObjectMapper()
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = ErrorResponse.from(errorCode)
        try {
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}