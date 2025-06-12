//package me.performancereservation.api;
//
//import me.performancereservation.domain.performance.entities.Performance;
//import me.performancereservation.domain.performance.entities.PerformanceSchedule;
//import me.performancereservation.domain.performance.enums.PerformanceCategory;
//import me.performancereservation.domain.performance.enums.PerformanceStatus;
//import me.performancereservation.domain.performance.repository.PerformanceRepository;
//import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
//import me.performancereservation.domain.reservation.ReservationRepository;
//import me.performancereservation.domain.user.entitiy.User;
//import me.performancereservation.domain.user.enums.Role;
//import me.performancereservation.domain.user.repository.UserRepository;
//import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@Transactional
//@Rollback
//@ActiveProfiles("test")
//@AutoConfigureMockMvc
//class ManagerPerformanceControllerTest {
//
//    @Autowired
//    MockMvc mockMvc;
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    ReservationRepository reservationRepository;
//
//    @Autowired
//    PerformanceRepository performanceRepository;
//
//    @Autowired
//    PerformanceScheduleRepository performanceScheduleRepository;
//
//    User manager;
//    CustomOAuth2User principal;
//
//    @BeforeEach
//    void init() {
//        performanceScheduleRepository.deleteAll();
//        performanceRepository.deleteAll();
//        userRepository.deleteAll();
//
//        manager = userRepository.save(
//                User.builder()
//                        .role(Role.MANAGER)
//                        .name("매니저")
//                        .email("manager@gmail.com")
//                        .phoneNumber("010-2345-2345")
//                        .build()
//        );
//
//        principal = new CustomOAuth2User(manager, Collections.emptyMap());
//
//        for (int i = 1; i <= 15; i++) {
//            Performance performance = performanceRepository.save(
//                    Performance.builder()
//                            .title("오페라 갈라 " + i)
//                            .venue("세종문화회관 대극장")
//                            .price(120000 + i * 1000)
//                            .totalSeats(2000)
//                            .category(PerformanceCategory.OPERA)
//                            .startDate(LocalDateTime.of(2025, 12, 12, 0, 0).plusDays(i))
//                            .endDate(LocalDateTime.of(2025, 12, 15, 0, 0).plusDays(i))
//                            .description("오페라 명곡과 스타들! " + i)
//                            .managerId(manager.getId())
//                            .status(i <= 10 ? PerformanceStatus.CONFIRMED : PerformanceStatus.COMPLETED) // 10개는 진행중, 5개는 완료
//                            .build()
//            );
//
//            performanceScheduleRepository.save(
//                    PerformanceSchedule.builder()
//                            .performanceId(performance.getId())
//                            .startTime(performance.getStartDate().withHour(13).withMinute(0).withSecond(0).withNano(0))
//                            .endTime(performance.getStartDate().withHour(15).withMinute(0).withSecond(0).withNano(0))
//                            .remainingSeats(performance.getTotalSeats())
//                            .canceled(false)
//                            .build()
//            );
//
//            performanceScheduleRepository.save(
//                    PerformanceSchedule.builder()
//                            .performanceId(performance.getId())
//                            .startTime(performance.getStartDate().withHour(17).withMinute(0).withSecond(0).withNano(0))
//                            .endTime(performance.getStartDate().withHour(19).withMinute(0).withSecond(0).withNano(0))
//                            .remainingSeats(performance.getTotalSeats())
//                            .canceled(false)
//                            .build()
//            );
//        }
//    }
//
//    @Test
//    void getPerformancesPaging_Success() throws Exception {
//        mockMvc.perform(get("/api/v1/managers/performances")
//                        .with(authentication(new OAuth2AuthenticationToken(principal, principal.getAuthorities(),"google"))) // 인증 추가
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$.content.size()").value(10))
//                .andExpect(jsonPath("$.totalElements").value(15))
//                .andExpect(jsonPath("$.totalPages").value(2));
//    }
//
//    @Test
//    void searchPerformanceByTitle_Success() throws Exception {
//        // "오페라 갈라 1"이 제목에 포함된 공연 검색
//        mockMvc.perform(get("/api/v1/managers/performances/search")
//                        .param("title", "오페라 갈라 1")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .with(authentication(new OAuth2AuthenticationToken(principal, principal.getAuthorities(),"google"))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.size()").value(7))
//                .andExpect(jsonPath("$.totalElements").value(7))
//                .andExpect(jsonPath("$.totalPages").value(1));
//    }
//
//    @Test
//    void searchPerformanceByVenue_Success() throws Exception {
//        // "세종문화회관"이 공연장에 포함된 공연 검색
//        mockMvc.perform(get("/api/v1/managers/performances/search")
//                        .param("venue", "세종문화회관")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .with(authentication(new OAuth2AuthenticationToken(principal, principal.getAuthorities(),"google"))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.size()").value(10))
//                .andExpect(jsonPath("$.totalElements").value(15))
//                .andExpect(jsonPath("$.totalPages").value(2));
//    }
//
//    @Test
//    void searchPerformanceByDate_Success() throws Exception {
//        // 2025-12-15 ~ 2025-12-20 사이 공연 검색
//        mockMvc.perform(get("/api/v1/managers/performances/search")
//                        .param("start", "2025-12-15T00:00:00")
//                        .param("end", "2025-12-20T23:59:59")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .with(authentication(new OAuth2AuthenticationToken(principal, principal.getAuthorities(),"google"))))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content.length()").value(8))
//                .andExpect(jsonPath("$.totalElements").value(8))
//                .andExpect(jsonPath("$.totalPages").value(1));
//    }
//}