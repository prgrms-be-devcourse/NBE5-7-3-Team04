package me.performancereservation.global.config;

import lombok.RequiredArgsConstructor;
import me.performancereservation.global.security.jwt.JwtAuthenticationFilter;
import me.performancereservation.global.security.jwt.JwtExceptionHandlerFilter;
import me.performancereservation.global.security.jwt.JwtTokenProvider;
import me.performancereservation.global.security.oauth.service.CustomOAuth2UserService;
import me.performancereservation.global.security.oauth.handler.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionHandlerFilter jwtExceptionHandlerFilter;

    //비밀번호 암호화용 PasswordEncoder 빈 등록 : 소셜로그인만 써도 확장성/관례상 등록해주기!!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //SecurityFilterChain : 인증/인가, OAuth2, JWT, CORS, CSRF 등 모든 정책을 한 곳에서 관리
    @Bean
    public SecurityFilterChain oauthSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // token 사용 -> csrf 필요 없음
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) //폼로그인 끄기
                .cors(cors ->{})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**","swagger-ui/index.html#/","/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/api/v1/users/sign-in","/api/v1/users/sign-up","/api/v1/auth/reissue").permitAll() //인증 없이 접근 허용(사이트 구경은 가능)
                        .requestMatchers("/api/v1/reservation/**").authenticated() //JWT 인증 필요 : 예약은 회원만..
                        .requestMatchers( "/oauth2/**","/oauth2/authorize/**", "/login/oauth2/code/**").permitAll()
                        .anyRequest().permitAll() //테스트용으로 일단 전부 허용 이후 수정!!
                        //.anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                //JWT 인증 필터는 UsernamePasswordAuthenticationFilter 앞에
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                //예외처리 필터는 JWT 인증 필터 앞에
                .addFilterBefore(jwtExceptionHandlerFilter, JwtAuthenticationFilter.class)
        ;
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**") // Swagger 관련 요청 무시
                .requestMatchers("/error", "/favicon.ico"); // oauth 시 리다이렉션 되는 url 경로 무시
    }
}
