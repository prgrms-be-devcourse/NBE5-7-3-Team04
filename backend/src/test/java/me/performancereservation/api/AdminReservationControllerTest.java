package me.performancereservation.api;

import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AdminReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    PerformanceRepository performanceRepository;

    @Autowired
    PerformanceScheduleRepository performanceScheduleRepository;

    User user5;
    User user6;
    User user7;
    User user8;
    User user9;

    @BeforeEach
    void init() {
        User user1 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자1")
                        .email("user1@gmail.com")
                        .phoneNumber("010-1111-1111")
                        .build()
        );
        User user2 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자2")
                        .email("user2@gmail.com")
                        .phoneNumber("010-2222-2222")
                        .build()
        );
        User user3 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자3")
                        .email("user3@gmail.com")
                        .phoneNumber("010-3333-3333")
                        .build()
        );
        User user4 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자4")
                        .email("user4@gmail.com")
                        .phoneNumber("010-3333-3333")
                        .build()
        );
        user5 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자5")
                        .email("user5@gmail.com")
                        .phoneNumber("010-5555-5555")
                        .build()
        );
        user6 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자6")
                        .email("user6@gmail.com")
                        .phoneNumber("010-6666-6666")
                        .build()
        );
        user7 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자7")
                        .email("user7@gmail.com")
                        .phoneNumber("010-7777-7777")
                        .build()
        );
        user8 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자8")
                        .email("user8@gmail.com")
                        .phoneNumber("010-8888-8888")
                        .build()
        );

        user9 = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .name("사용자9")
                        .email("user9@gmail.com")
                        .phoneNumber("010-9999-9999")
                        .build()
        );

        User manager = userRepository.save(
                User.builder()
                        .role(Role.MANAGER)
                        .name("매니저")
                        .email("manager@gmail.com")
                        .phoneNumber("010-2345-2345")
                        .build()
        );

        Performance performance = performanceRepository.save(
                Performance.builder()
                        .title("오페라 갈라")
                        .venue("세종문화회관 대극장")
                        .price(120000)
                        .totalSeats(2000)
                        .category(PerformanceCategory.OPERA)
                        .performanceDate(LocalDateTime.of(2025, 12, 13, 0, 0))
                        .description("한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!")
                        .managerId(manager.getId())
                        .status(PerformanceStatus.CONFIRMED)
                        .build()
        );

        PerformanceSchedule schedule = performanceScheduleRepository.save(
                PerformanceSchedule.builder()
                        .performanceId(performance.getId())
                        .startTime(LocalDateTime.of(2025, 12, 13, 9, 0))
                        .endTime(LocalDateTime.of(2025, 12, 13, 10, 0))
                        .remainingSeats(performance.getTotalSeats())
                        .canceled(false)
                        .build()
        );

        List<User> users = List.of(user1, user2, user3, user4);
        for (int i = 0; i < 50; i++) {
            ReservationStatus status = i % 2 == 0
                    ? ReservationStatus.PAYMENTS_PENDING
                    : ReservationStatus.PAYMENTS_CONFIRMED;


            reservationRepository.save(
                    Reservation.builder()
                            .userId(users.get(i % users.size()).getId())
                            .quantity((i % 4) + 1) // 1~4개
                            .scheduleId(schedule.getId())
                            .status(status)
                            .build()
            );
        }

        reservationRepository.save(
                Reservation.builder()
                        .userId(user5.getId())
                        .quantity(2)
                        .scheduleId(schedule.getId())
                        .status(ReservationStatus.PAYMENTS_PENDING)
                        .build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .userId(user6.getId())
                        .quantity(1)
                        .scheduleId(schedule.getId())
                        .status(ReservationStatus.PAYMENTS_CONFIRMED)
                        .build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .userId(user7.getId())
                        .quantity(1)
                        .scheduleId(schedule.getId())
                        .status(ReservationStatus.PAYMENTS_PENDING)
                        .build()
        );

        reservationRepository.save(
                Reservation.builder()
                        .userId(user8.getId())
                        .quantity(1)
                        .scheduleId(schedule.getId())
                        .status(ReservationStatus.PAYMENTS_PENDING)
                        .build()
        );

        reservationRepository.save(
                Reservation.builder()
                        .userId(user9.getId())
                        .quantity(1)
                        .scheduleId(schedule.getId())
                        .status(ReservationStatus.CANCEL_PENDING) //CANCEL_PENDING 검색 테스트용
                        .build()
        );
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getReservationListPaging_Success() throws Exception {
        mockMvc.perform(get("/api/v1//admin/reservations")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(10)) // 1페이지 10개
                .andExpect(jsonPath("$.totalPages").value(6)) // 54 / 10 = 5.4 -> 6페이지
                .andExpect(jsonPath("$.totalElements").value(55)); // 총 50개
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByName_Success() throws Exception {
        // 사용자1의 예약 수 = 50 / 4 = 12.5 → 13개
        mockMvc.perform(get("/api/v1//admin/reservations/search")
                        .param("userName", "사용자1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(10)) // 1페이지 10개
                .andExpect(jsonPath("$.totalElements").value(13)) // 총 13개
                .andExpect(jsonPath("$.totalPages").value(2)); // 13 / 10 = 1.3 → 2페이지
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByName_Failure() throws Exception {
        // 사용자1의 예약 수 = 50 / 4 = 12.5 → 13개
        mockMvc.perform(get("/api/v1//admin/reservations/search")
                        .param("userName", "없는 사용자")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByTitle_Success() throws Exception {
        // 사용자1의 예약 수 = 50 / 4 = 12.5 → 13개
        mockMvc.perform(get("/api/v1//admin/reservations/search")
                        .param("performanceTitle", "오페라 갈라")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(10)) // 1페이지 10개
                .andExpect(jsonPath("$.totalElements").value(55)) // 총 50개
                .andExpect(jsonPath("$.totalPages").value(6)); // 54 / 10 = 5.4 → 6페이지
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByTitle_Failure() throws Exception {
        // 사용자1의 예약 수 = 50 / 4 = 12.5 → 13개
        mockMvc.perform(get("/api/v1//admin/reservations/search")
                        .param("performanceTitle", "없는 공연")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByStatus_PAYMENT_PENDING() throws Exception {
        // PAYMENTS_PENDING: 50 / 2 = 25개
        mockMvc.perform(get("/api/v1/admin/reservations/search")
                        .param("reservationStatus", "PAYMENTS_PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(10))
                .andExpect(jsonPath("$.totalElements").value(28)) // 25개
                .andExpect(jsonPath("$.totalPages").value(3)); // 26 / 10 = 2.6 → 3페이지
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByStatus_CANCELPENDING() throws Exception {
        // PAYMENTS_PENDING: 50 / 2 = 25개
        mockMvc.perform(get("/api/v1/admin/reservations/search")
                        .param("reservationStatus", "CANCEL_PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByDate_Success() throws Exception {
        // 1일 전 ~ 1일 후 (모든 예약 포함)
        LocalDateTime start = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now().plusDays(1).toLocalDate().atTime(23, 59, 59);

        mockMvc.perform(get("/api/v1/admin/reservations/search")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(10))
                .andExpect(jsonPath("$.totalElements").value(55)) // 전체 50개
                .andExpect(jsonPath("$.totalPages").value(6));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchReservationByDate_Failure() throws Exception {
        // 11일 전 ~ 10일 전 (예약 없음)
        LocalDateTime start = LocalDateTime.now().minusDays(11).toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now().minusDays(10).toLocalDate().atTime(23, 59, 59);

        mockMvc.perform(get("/api/v1//admin/reservations/search")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmReservation_Success() throws Exception {
        // 펜딩 상태 예약 확정 성공
        Reservation pending = reservationRepository.findByUserId(user5.getId()).get();

        mockMvc.perform(patch("/api/v1//admin/reservations/" + pending.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        PerformanceSchedule schedule = performanceScheduleRepository.findById(pending.getScheduleId()).get();
        Performance performance = performanceRepository.findById(schedule.getPerformanceId()).get();

        // 상태가 확정으로 변경되었는지 확인
        Reservation updated = reservationRepository.findById(pending.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.PAYMENTS_CONFIRMED);
        // 좌석 차감 확인
        assertThat(schedule.getRemainingSeats()).isEqualTo(performance.getTotalSeats() - pending.getQuantity());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmReservationAlreadyConfirmed_Failure() throws Exception {
        // 이미 확정된 예약에 대해 확정 요청 시 409 CONFLICT
        Reservation confirmed = reservationRepository.findByUserId(user6.getId()).get();

        mockMvc.perform(patch("/api/v1/admin/reservations/" + confirmed.getId())
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 확정된 예약입니다."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmReservationCanceledStatus_Failure() throws Exception {
        // 취소 상태 예약에 대해 확정 요청 시 400 BAD_REQUEST
        Reservation cancelPending = reservationRepository.findByUserId(user7.getId()).get();
        cancelPending.requestCancel();

        mockMvc.perform(patch("/api/v1/admin/reservations/" + cancelPending.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 예약 상태입니다."));

        Reservation cancelConfirmed = reservationRepository.findByUserId(user8.getId()).get();
        cancelConfirmed.cancelConfirm();

        mockMvc.perform(patch("/api/v1/admin/reservations/" + cancelConfirmed.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 예약 상태입니다."));
    }

}


