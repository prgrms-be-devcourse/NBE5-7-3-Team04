package me.performancereservation.global.config;

import lombok.RequiredArgsConstructor;
import me.performancereservation.global.security.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    //비밀번호 암호화용 PasswordEncoder 빈 등록 : 소셜로그인만 써도 확장성/관례상 등록해주기!!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //SecurityFilterChain : 인증/인가, OAuth2, JWT, CORS, CSRF 등 모든 정책을 한 곳에서 관리
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // token 사용 -> csrf 필요 없음
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors ->{})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**","swagger-ui/index.html#/","/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/api/v1/users/sign-in","/api/v1/users/sign-up").permitAll() //인증 없이 접근 허용(사이트 구경은 가능)
                        .requestMatchers("/api/v1/reservation/**").authenticated() //JWT 인증 필요 : 예약은 회원만..
                        // .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") //어드민(세션) 구현 예정
                        .anyRequest().permitAll() //테스트용으로 일단 전부 허용, 이후 수정하겠습니다!!
                        //.anyRequest().authenticated()
                );
        return http.build();
    }

}
