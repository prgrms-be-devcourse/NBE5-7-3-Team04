package me.performancereservation.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {
    //엑세스 토큰과 리프레쉬 토큰이 존재한다.
    //지금은 엑세스 토큰만 우선 구현

    private final Key key;
    private final long accessExpiration;
    @Getter
    private final long refreshExpiration;
    private final UserService userService;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration,
            UserService userService
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.userService = userService;
    }

    //인증된 User 정보로 JWT 토큰을 발급
    public String createAccessToken(User user) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //리프레쉬 토큰 발급
    public String createRefreshToken(User user) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //JWT 토큰에서 인증 객체를 추출. SecurityContextHolder 에 담을 객체!!
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long userId = Long.valueOf(claims.getSubject());

        // JWT 기반 인증 시, DB에서 유저 조회하여 CustomOAuth2User 생성
        User user = userService.getUserById(userId);
        CustomOAuth2User principal = new CustomOAuth2User(user, Map.of());

        //UserDetails 대신 직접 Principal 정보 구성
        return new UsernamePasswordAuthenticationToken(
                principal,
                null, // Credentials(비밀번호 없음)
                principal.getAuthorities()
        );
    }

    //JWT 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    // 엑세스 토큰의 유효성 검증
    //유효성 검증을 access,refresh 모두 하나의 검사로 처리하고 싶어서 두개를 만들기 보다는 하나를 변경해보았습니다.
    public Claims validateToken(String token, ErrorCode errorCode) {
        try {
                return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw errorCode.serviceException();
        }
    }

    // http 헤더로부터 bearer 토큰 분리
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //JWT 토큰에서 Claims(내부 정보) 추출. 만료된 토큰도 Claims는 추출 가능
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
