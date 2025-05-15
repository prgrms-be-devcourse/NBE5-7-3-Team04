package me.performancereservation.global.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.performancereservation.global.security.jwt.JwtAuthenticationFilter;
import me.performancereservation.global.security.jwt.JwtExceptionHandlerFilter;
import me.performancereservation.global.security.jwt.JwtTokenProvider;
import me.performancereservation.global.security.oauth.service.CustomOAuth2UserService;
import me.performancereservation.global.security.oauth.handler.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class UserSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionHandlerFilter jwtExceptionHandlerFilter;


    //SecurityFilterChain : 인증/인가, OAuth2, JWT, CORS, CSRF 등 모든 정책을 한 곳에서 관리
    @Bean
    @Order(2)
    public SecurityFilterChain oauthSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // token 사용 -> csrf 필요 없음
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**","swagger-ui/index.html#/","/v3/api-docs/**",
                                "/api/v1/health-check", "/swagger-resources/**").permitAll()

                        //공통 서비스
                        .requestMatchers("/api/v1/auth/signup-test","/api/v1/auth/reissue","/api/v1/auth/logout",
                                "/api/v1/auth/token-test", "/oauth2/**", "/login/oauth2/code/**").permitAll()

                        // 유저만
                        .requestMatchers("/api/v1/users/manager-request").hasRole("USER")

                        // 공연관리자만
                        .requestMatchers("/api/v1/files/**","/api/v1/managers/**").hasRole("MANAGER")

                        // 유저+공연관리자
                        .requestMatchers("/api/v1/users/me", "/api/v1/users/onboarding", "/api/v1/reviews",
                                "/api/v1/reservations/**","/api/v1/bookmark/**","/api/v1/refunds/**"
                        ).hasAnyRole("USER", "MANAGER")

                        // 모두 접근 가능 (공연 목록/상세/검색 등)
                        .requestMatchers("/api/v1/users/search", "/api/v1/users/performances/**", "/api/v1/reviews/**").permitAll()

                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                //JWT 인증 필터는 UsernamePasswordAuthenticationFilter 앞에
                //토큰을 주입하는 역할. 인증에 성공하면 컨트롤러에서 사용 가능
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                //예외처리 필터는 JWT 인증 필터 앞에
                //발급된 토큰의 인증 과정에서 유효한지 검사(만료, 변조 등등)
                //안에서 try-catch문으로 확인하고 27번 줄에서 그 외의 예외인 경우 catch하지 않고 throws ServletException, IOException
                //try-catch문으로 작성할 수도 있으며 catch로 오류를 잡을 경우 이후 exceptionHandling 작동 x, 정상 빌드로 넘어감
                //throw 로 예외 발생 시 exceptionHandling으로 넘어가서 작동
                .addFilterBefore(jwtExceptionHandlerFilter, JwtAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            //CustomAuthenticationEntryPoint로 보통 정리하지만 리팩토링 시 적용하겠습니다.
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"로그인을 해주세요.\"}");
                        })
                );
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**") // Swagger 관련 요청 무시
                .requestMatchers("/error", "/favicon.ico"); // oauth 시 리다이렉션 되는 url 경로 무시
    }
}
