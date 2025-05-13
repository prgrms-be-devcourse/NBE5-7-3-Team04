package me.performancereservation.global.config;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.admin.AdminService;
import me.performancereservation.domain.admin.handler.AdminAuthSuccessHandler;
import me.performancereservation.domain.admin.handler.CustomAccessDeniedHandler;
import me.performancereservation.domain.admin.handler.CustomAuthenticationEntryPoint;
import me.performancereservation.domain.admin.handler.CustomAuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final AdminService adminService;

    private final AdminAuthSuccessHandler adminAuthSuccessHandler;                          // 로그인 성공 핸들러
    private final CustomAccessDeniedHandler customAccessDeniedHandler;                      // 권한이 없는 경우를 처리하는 핸들러
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;            // 로그인이 안된 경우를 처리하는 핸들러
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;    // 로그인 실패 핸들러

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1) // 사용자 SecurityConfig 보다 먼저 적용되도록 Order 값 설정
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(crfs -> crfs.disable())
                .cors(cors -> cors.disable())
                .securityMatcher("/api/v1/admin/**") // 관리자 기능에만 AdminSecurity 를 적용하여 사용자와 분리
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin/login")
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/v1/admin/login")          // 로그인 처리 URL
                        .successHandler(adminAuthSuccessHandler)            // 로그인 성공 핸들러
                        .failureHandler(customAuthenticationFailureHandler) // 로그인 실패 핸들러
                        .usernameParameter("adminId")
                        .passwordParameter("password")
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/v1/admin/logout"))
                        .invalidateHttpSession(true)                            //세션 삭제
                        .deleteCookies("JSESSIONID")    //쿠키 삭제
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)                 // 중복 로그인 방지
                        .maxSessionsPreventsLogin(false)    // 새 로그인이 이전 세션 만료시킴
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)   // 인증 필요 시 커스텀 예외
                        .accessDeniedHandler(customAccessDeniedHandler)             // 접근 거부 시 커스텀 예외
                )
                .userDetailsService(adminService)
                .build();
    }
}
