package me.performancereservation.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenProvider {
    //엑세스 토큰과 리프레쉬 토큰이 존재한다.
    //지금은 엑세스 토큰만 우선 구현

    private final Key key;
    private final long accessExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${access-expiration}") long accessExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpiration = accessExpiration;
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

    //JWT 토큰에서 인증 객체를 추출. SecurityContextHolder 에 담을 객체!!
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);

        //UserDetails 대신 직접 Principal 정보 구성
        return new UsernamePasswordAuthenticationToken(
                userId, // Principal(userId)
                null, // Credentials(비밀번호 없음)
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    //JWT 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    // JWT 토큰의 유효성 검증
    public boolean validateToken(String token) {
        try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_JWT_TOKEN,ErrorType.DOMAIN);
            //로그만 받을지, 유효성 검사 후 메시지 출력하는 함수를 따로 만들지,
            //parseClaimsJws 내부에서 자동으로 이루어지는 유효성 검사를 모두 밖으로 분리해서 try-catch문으로 하나씩 작성할지 고민이 되네요
            //어떤 방식이 좋을지 리뷰부탁드립니다.
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
