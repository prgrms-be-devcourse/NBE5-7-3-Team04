package me.performancereservation.global.security.admin.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.exception.ErrorResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    //어드민의 인증정보(아이디, 비밀번호)가 틀린 경우의 예외를 처리하는 핸들러
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.ADMIN_NOT_FOUND;
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        String adminId = request.getParameter("adminId");
        String clientIp = getClientIp(request);

        log.error("Admin login failed - ID: {}, IP: {}, Reason: {}",
                adminId, clientIp, exception.getMessage(), exception);

        //response에 예외를 셋팅해 반환해 줍니다.
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
