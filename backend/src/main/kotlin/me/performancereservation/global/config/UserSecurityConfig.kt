package me.performancereservation.global.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.performancereservation.global.security.jwt.JwtAuthenticationFilter
import me.performancereservation.global.security.jwt.JwtExceptionHandlerFilter
import me.performancereservation.global.security.oauth.handler.OAuth2SuccessHandler
import me.performancereservation.global.security.oauth.service.CustomOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class UserSecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtExceptionHandlerFilter: JwtExceptionHandlerFilter
) {
    @Bean
    @Order(2)
    fun oauthSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http // token 사용 -> csrf 필요 없음
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
            .httpBasic { obj: HttpBasicConfigurer<HttpSecurity> -> obj.disable() }
            .formLogin { obj: FormLoginConfigurer<HttpSecurity> -> obj.disable() }
            .cors(Customizer.withDefaults())
            .sessionManagement { session: SessionManagementConfigurer<HttpSecurity?> ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            } // 세션 사용 안함
            .authorizeHttpRequests(Customizer { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**", "swagger-ui/index.html#/", "/v3/api-docs/**", "/docs",
                        "/api/v1/health-check", "/swagger-resources/**"
                    ).permitAll() //공통 서비스

                    .requestMatchers(
                        "/api/v1/auth/signup-test", "/api/v1/auth/reissue", "/api/v1/auth/logout",
                        "/api/v1/auth/token-test", "/oauth2/**", "/login/oauth2/code/**"
                    ).permitAll() // 유저만

                    .requestMatchers("/api/v1/users/manager-request").hasRole("USER") // 공연관리자만

                    .requestMatchers("/api/v1/files/**", "/api/v1/managers/**").hasRole("MANAGER") // 유저+공연관리자

                    .requestMatchers(
                        "/api/v1/users/me",
                        "/api/v1/users/onboarding",
                        "/api/v1/reviews",
                        "/api/v1/reservations/**",
                        "/api/v1/bookmark/**",
                        "/api/v1/refunds/**",
                        "/api/v1/users/manager-status"
                    ).hasAnyRole("USER", "MANAGER") // 모두 접근 가능 (공연 목록/상세/검색 등)

                    .requestMatchers("/api/v1/users/search", "/api/v1/users/performances/**", "/api/v1/reviews/**")
                    .permitAll()
                    .anyRequest().permitAll()
            }
            )
            .oauth2Login { oauth: OAuth2LoginConfigurer<HttpSecurity?> ->
                oauth
                    .userInfoEndpoint(Customizer { userInfo ->
                        userInfo.userService(customOAuth2UserService)
                    }
                    )
                    .successHandler(oAuth2SuccessHandler)
            } //JWT 인증 필터는 UsernamePasswordAuthenticationFilter 앞에
            //토큰을 주입하는 역할. 인증에 성공하면 컨트롤러에서 사용 가능
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            ) //예외처리 필터는 JWT 인증 필터 앞에
            //발급된 토큰의 인증 과정에서 유효한지 검사(만료, 변조 등등)
            //안에서 try-catch문으로 확인하고 27번 줄에서 그 외의 예외인 경우 catch하지 않고 throws ServletException, IOException
            //try-catch문으로 작성할 수도 있으며 catch로 오류를 잡을 경우 이후 exceptionHandling 작동 x, 정상 빌드로 넘어감
            //throw 로 예외 발생 시 exceptionHandling으로 넘어가서 작동
            .addFilterBefore(jwtExceptionHandlerFilter, JwtAuthenticationFilter::class.java)
            .exceptionHandling { exception: ExceptionHandlingConfigurer<HttpSecurity?> ->
                exception
                    .authenticationEntryPoint { request: HttpServletRequest?, response: HttpServletResponse, authException: AuthenticationException? ->
                        //CustomAuthenticationEntryPoint로 보통 정리하지만 리팩토링 시 적용하겠습니다.
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.contentType = "application/json"
                        response.writer.write("{\"error\": \"로그인을 해주세요.\"}")
                    }
            }
        return http.build()
    }

    @Bean
    fun ignoringCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web: WebSecurity ->
            web.ignoring()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**") // Swagger 관련 요청 무시
                .requestMatchers("/error", "/favicon.ico")
        } // oauth 시 리다이렉션 되는 url 경로 무시
    }
}