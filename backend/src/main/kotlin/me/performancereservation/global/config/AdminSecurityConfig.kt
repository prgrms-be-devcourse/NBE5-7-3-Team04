package me.performancereservation.global.config

import me.performancereservation.global.security.admin.handler.AdminAuthSuccessHandler
import me.performancereservation.global.security.admin.handler.CustomAccessDeniedHandler
import me.performancereservation.global.security.admin.handler.CustomAuthenticationEntryPoint
import me.performancereservation.global.security.admin.handler.CustomAuthenticationFailureHandler
import me.performancereservation.global.security.admin.service.AdminUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class AdminSecurityConfig(
    private val adminUserDetailsService: AdminUserDetailsService,
    private val adminAuthSuccessHandler: AdminAuthSuccessHandler,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAuthenticationFailureHandler: CustomAuthenticationFailureHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    @Order(1) // 사용자 SecurityConfig 보다 먼저 적용되도록 Order 값 설정
    fun adminSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { csrf ->
                csrf
                    .ignoringRequestMatchers("/api/v1/admin/login") // 로그인은 csrf 설정 제외
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // CSRF 토큰을 쿠키로 제공
            }
            .cors(Customizer.withDefaults()) // 전역 CORS 설정 사용
            .securityMatcher("/api/v1/admin/**") // 어드민 기능에만 AdminSecurity 를 적용하여 사용자와 분리
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/admin/login", "/api/v1/admin/check-auth")
                    .permitAll()
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated()
            }
            .formLogin { form ->
                form
                    .loginProcessingUrl("/api/v1/admin/login") // 로그인 처리 URL
                    .successHandler(adminAuthSuccessHandler) // 로그인 성공 핸들러
                    .failureHandler(customAuthenticationFailureHandler) // 로그인 실패 핸들러
                    .usernameParameter("adminId")
                    .passwordParameter("password")
            }
            .logout { logout ->
                logout
                    .logoutRequestMatcher(AntPathRequestMatcher("/api/v1/admin/logout"))
                    .invalidateHttpSession(true) //세션 삭제
                    .deleteCookies("JSESSIONID") //쿠키 삭제
            }
            .sessionManagement { session ->
                session
                    .maximumSessions(1) // 중복 로그인 방지
                    .maxSessionsPreventsLogin(false) // 새 로그인이 이전 세션 만료시킴
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint) // 로그인이 안된 경우를 처리하는 핸들러
                    .accessDeniedHandler(customAccessDeniedHandler) // 권한이 없는 경우를 처리하는 핸들러
            }
            .userDetailsService(adminUserDetailsService)
            .build()
    }
}