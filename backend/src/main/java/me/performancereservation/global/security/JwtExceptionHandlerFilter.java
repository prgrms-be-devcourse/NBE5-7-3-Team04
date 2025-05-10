package me.performancereservation.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.exception.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AppException e) {
            log.warn("[JwtExceptionHandlerFilter] AppException 발생: {}", e.getDeveloperMessage());
            setErrorResponse(response, e.getErrorCode());

        } catch (ExpiredJwtException e) { //토큰 만료
            setErrorResponse(response, ErrorCode.ACCESS_TOKEN_EXPIRED);
            log.info("[JwtExceptionHandlerFilter] error name = ExpiredJwtException");

        } catch (IllegalArgumentException e) { // 잘못된 인수나 인수의 값이 올바르지 않은 경우
            setErrorResponse(response, ErrorCode.INVALID_JWT_TOKEN);
            log.info("[JwtExceptionHandlerFilter] error name = IllegalArgumentException");

        } catch (NullPointerException e) { //토큰이 NULL인 경우(헤더 누락 등)
            setErrorResponse(response, ErrorCode.TOKEN_NOT_FOUND);
            log.info("[JwtExceptionHandlerFilter] error name = NullPointerException");

        } catch (Exception e) { //기타 오류 발생
            setErrorResponse(response, ErrorCode.INVALID_JWT_TOKEN);
            log.info("[JwtExceptionHandlerFilter] error name = UNKNOWN_Exception");

        }
    }

    private void setErrorResponse(HttpServletResponse response,
                                  ErrorCode errorCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}