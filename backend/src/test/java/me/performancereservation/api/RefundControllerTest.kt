//package me.performancereservation.api
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import io.mockk.every
//import io.mockk.mockk
//import me.performancereservation.domain.refund.RefundService
//import me.performancereservation.domain.refund.dto.RefundDetailResponse
//import me.performancereservation.domain.refund.dto.RefundResponse
//import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest
//import me.performancereservation.domain.refund.enums.RefundStatus
//import me.performancereservation.domain.user.entitiy.User
//import me.performancereservation.domain.user.enums.Role
//import me.performancereservation.global.security.oauth.user.CustomOAuth2User
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.data.domain.PageImpl
//import org.springframework.data.domain.PageRequest
//import org.springframework.data.domain.Sort
//import org.springframework.http.MediaType
//import org.springframework.security.test.context.support.WithSecurityContext
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
//import java.time.LocalDateTime
//import org.springframework.security.test.context.support.WithSecurityContextFactory
//import org.springframework.security.core.context.SecurityContext
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.security.oauth2.core.user.OAuth2User
//import org.springframework.security.test.context.support.WithMockUser
//import org.springframework.security.core.Authentication
//import org.springframework.security.core.context.SecurityContextImpl
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
//import org.springframework.security.config.annotation.web.builders.HttpSecurity
//import org.springframework.security.core.authority.SimpleGrantedAuthority
//import org.springframework.security.web.SecurityFilterChain
//import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
//import org.springframework.test.web.servlet.setup.MockMvcBuilders
//import org.springframework.web.context.WebApplicationContext
//import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
//import org.springframework.security.oauth2.core.oidc.OidcIdToken
//import org.springframework.security.oauth2.core.oidc.OidcUserInfo
//import org.springframework.security.oauth2.core.oidc.user.OidcUser
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
//import java.time.Instant
//import java.util.*
//
//@WebMvcTest(RefundController::class)
//class RefundControllerTest {
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//
//    @Autowired
//    private lateinit var webApplicationContext: WebApplicationContext
//
//    private lateinit var refundService: RefundService
//    private lateinit var customOAuth2User: CustomOAuth2User
//    private lateinit var user: User
//
//    companion object {
//        private const val USER_ID = 1L
//        private const val RESERVATION_ID = 1L
//        private const val REFUND_ID = 1L
//    }
//
//    @BeforeEach
//    fun setUp() {
//        refundService = mockk()
//        user = User.builder()
//            .id(USER_ID)
//            .email("test@example.com")
//            .name("테스트")
//            .phoneNumber("010-1234-5678")
//            .role(Role.USER)
//            .build()
//        customOAuth2User = CustomOAuth2User(user, mapOf())
//    }
//
//    @Test
//    @DisplayName("사용자별 환불 내역 조회 성공 테스트")
//    fun getAllRefundDetailsWithUserId_Success() {
//        // given
//        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
//        val refundDetailResponse = RefundDetailResponse(
//            refundId = REFUND_ID,
//            userId = USER_ID,
//            reservationId = RESERVATION_ID,
//            account = "123-456-789",
//            bank = "신한은행",
//            depositorName = "홍길동",
//            refundStatus = RefundStatus.PENDING,
//            createdDate = LocalDateTime.now(),
//            updatedDate = LocalDateTime.now(),
//            quantity = 2,
//            startTime = LocalDateTime.now(),
//            fileId = 1L,
//            title = "오페라 갈라",
//            venue = "세종문화회관 대극장",
//            price = 120000,
//            category = "MUSICAL_OPERA",
//            performanceDate = LocalDateTime.now(),
//            description = "한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!"
//        )
//        val page = PageImpl(listOf(refundDetailResponse))
//        every { refundService.findAllRefundsDetailByUserId(USER_ID, pageable) } returns page
//
//        // when & then
//        mockMvc.perform(
//            get("/api/v1/refunds/me")
//                .with(oauth2Login()
//                    .attributes { attrs ->
//                        attrs["sub"] = USER_ID.toString()
//                        attrs["email"] = user.email
//                        attrs["name"] = user.name
//                    }
//                    .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
//                )
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.content[0].refundId").value(REFUND_ID))
//            .andExpect(jsonPath("$.content[0].userId").value(USER_ID))
//            .andExpect(jsonPath("$.content[0].reservationId").value(RESERVATION_ID))
//    }
//
//    @Test
//    @DisplayName("예약별 환불 정보 조회 성공 테스트")
//    fun getRefund_Success() {
//        // given
//        val refundResponse = RefundResponse(
//            refundId = REFUND_ID,
//            reservationId = RESERVATION_ID,
//            userId = USER_ID,
//            account = "123-456-789",
//            bank = "신한은행",
//            depositorName = "홍길동",
//            status = RefundStatus.PENDING
//        )
//        every { refundService.findRefundByUserId(USER_ID, RESERVATION_ID) } returns refundResponse
//
//        // when & then
//        mockMvc.perform(
//            get("/api/v1/refunds/$RESERVATION_ID")
//                .with(oauth2Login()
//                    .attributes { attrs ->
//                        attrs["sub"] = USER_ID.toString()
//                        attrs["email"] = user.email
//                        attrs["name"] = user.name
//                    }
//                    .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
//                )
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.refundId").value(REFUND_ID))
//            .andExpect(jsonPath("$.reservationId").value(RESERVATION_ID))
//            .andExpect(jsonPath("$.userId").value(USER_ID))
//    }
//
//    @Test
//    @DisplayName("환불 계좌 정보 업데이트 성공 테스트")
//    fun updateBankInfo_Success() {
//        // given
//        val request = UpdateBankInfoRequest(
//            refundId = REFUND_ID,
//            account = "987-654-321",
//            bank = "국민은행",
//            depositorName = "김철수"
//        )
//
//        // when & then
//        mockMvc.perform(
//            patch("/api/v1/refunds")
//                .with(csrf())
//                .with(oauth2Login()
//                    .attributes { attrs ->
//                        attrs["sub"] = USER_ID.toString()
//                        attrs["email"] = user.email
//                        attrs["name"] = user.name
//                    }
//                    .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
//                )
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//        )
//            .andExpect(status().isOk)
//    }
//
//    @Configuration
//    class TestConfig {
//        @Bean
//        fun refundService(): RefundService = refundService()
//
//        @Bean
//        fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//            http
//                .csrf { it.disable() }
//                .authorizeHttpRequests { auth ->
//                    auth.anyRequest().authenticated()
//                }
//                .oauth2Login { }
//            return http.build()
//        }
//    }
//}