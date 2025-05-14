package me.performancereservation.global.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (isExcludedUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtTokenProvider.resolveToken(request); //Authorization 헤더에서 Bearer 토큰 추출

        //토큰이 유효하면 SecurityContext에 인증 정보 저장
        if(accessToken !=null) {
            Claims claims = jwtTokenProvider.validateToken(accessToken, ErrorCode.INVALID_ACCESS_TOKEN);
            Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        //다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    private boolean isExcludedUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/api/v1/auth/reissue") ||
                uri.equals("/api/v1/auth/login") ||
                uri.equals("/api/v1/auth/logout");
    }
}
