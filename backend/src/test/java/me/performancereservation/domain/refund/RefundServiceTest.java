package me.performancereservation.domain.refund;

import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.enums.RefundStatus;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class RefundServiceTest {

    @Autowired
    private RefundService refundService;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PerformanceScheduleRepository performanceScheduleRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    private Refund refund;
    private Performance performance;
    private PerformanceSchedule schedule;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        log.info("테스트 설정 시작");
        
        // Performance 생성
        performance = Performance.builder()
                .title("테스트 공연")
                .venue("테스트 공연장")
                .price(10000)
                .category(PerformanceCategory.OPERA)
                .description("테스트 공연 설명")
                .performance_date(LocalDateTime.now())
                .status(PerformanceStatus.CONFIRMED)
                .build();
        performance = performanceRepository.save(performance);
        log.info("Performance 저장 완료: id={}", performance.getId());

        // PerformanceSchedule 생성
        schedule = PerformanceSchedule.builder()
                .performanceId(performance.getId())
                .startTime(LocalDateTime.now().plusDays(7))
                .endTime(LocalDateTime.now().plusDays(7).plusHours(2))
                .remainingSeats(100)
                .is_canceled(false)
                .build();
        schedule = performanceScheduleRepository.save(schedule);
        log.info("PerformanceSchedule 저장 완료: id={}", schedule.getId());

        // Reservation 생성
        reservation = Reservation.builder()
                .userId(1L)
                .scheduleId(schedule.getId())
                .quantity(2)
                .status(ReservationStatus.PAYMENTS_CONFIRMED)
                .build();
        reservation = reservationRepository.save(reservation);
        log.info("첫 번째 예약 저장 완료: id={}", reservation.getId());

        refund = Refund.builder()
                .id(1L)
                .reservationId(reservation.getId())
                .userId(1L)
                .status(RefundStatus.READY)
                .build();
        log.info("테스트 설정 완료");
    }

    @Test
    @DisplayName("save 테스트")
    void saveTest() throws Exception {
        // given
        log.info("환불 저장 테스트 시작");

        // when
        refundService.save(reservation);
        log.info("환불 저장 완료");

        // then
        Refund savedRefund = refundRepository.findRefundByReservationId(reservation.getId()).orElseThrow();
        RefundTestUtils.logRefundEntity(savedRefund, "DB에서 조회한 환불 정보");

        assertThat(savedRefund.getReservationId()).isEqualTo(reservation.getId());
        assertThat(savedRefund.getUserId()).isEqualTo(reservation.getUserId());
        assertThat(savedRefund.getStatus()).isEqualTo(RefundStatus.PENDING);
        log.info("환불 저장 테스트 완료");
    }

    @Test
    @DisplayName("전체 환불 상세 정보 조회 테스트")
    void findAllRefundsDetailTest() throws Exception {
        // given
        log.info("환불 상세 정보 조회 테스트 시작");

        // 첫 번째 환불 저장
        refundService.save(reservation);
        log.info("첫 번째 환불 저장 완료");

        // 두 번째 예약 생성
        Reservation reservation2 = Reservation.builder()
                .userId(2L)
                .scheduleId(schedule.getId())
                .quantity(1)
                .status(ReservationStatus.PAYMENTS_CONFIRMED)
                .build();
        reservation2 = reservationRepository.save(reservation2);
        log.info("다른 유저id로 두 번째 예약 저장 완료: id={}", reservation2.getId());

        // 두 번째 환불 저장
        refundService.save(reservation2);
        log.info("두 번째 환불 저장 완료");

        // when
        Page<RefundDetailResponse> refundDetailResponses = refundService.findAllRefundsDetail(PageRequest.of(0,10));
        log.info("환불 상세 정보 조회 완료: size={}", refundDetailResponses.getTotalElements());

        // then
        assertThat(refundDetailResponses.getTotalElements()).isEqualTo(2);

        // 첫 번째 환불 상세 정보 검증
        RefundDetailResponse firstRefundDetail = refundDetailResponses.getContent().get(0);
        RefundTestUtils.logRefundDetailResponse(firstRefundDetail, "첫 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(firstRefundDetail, reservation, schedule, performance);

        // 두 번째 환불 상세 정보 검증
        RefundDetailResponse secondRefundDetail = refundDetailResponses.getContent().get(1);
        RefundTestUtils.logRefundDetailResponse(secondRefundDetail, "두 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(secondRefundDetail, reservation2, schedule, performance);

        log.info("환불 상세 정보 조회 테스트 완료");
    }

    @Test
    @DisplayName("특정 사용자의 환불 상세 정보 조회 테스트")
    void findAllRefundsDetailByUserIdTest() throws Exception {
        // given
        log.info("특정 사용자의 환불 상세 정보 조회 테스트 시작");
        Long userId = 1L;

        // 첫 번째 환불 저장
        refundService.save(reservation);
        log.info("userid {}의 첫 번째 환불 저장 완료", userId);

        // 두 번째 예약 생성 (같은 사용자)
        Reservation reservation2 = Reservation.builder()
                .userId(userId)
                .scheduleId(schedule.getId())
                .quantity(1)
                .status(ReservationStatus.PAYMENTS_CONFIRMED)
                .build();
        reservation2 = reservationRepository.save(reservation2);
        log.info("같은 사용자의 두 번째 예약 저장 완료: id={}", reservation2.getId());

        // 두 번째 환불 저장
        refundService.save(reservation2);
        log.info("두 번째 환불 저장 완료");

        // 다른 사용자의 예약 생성
        Reservation otherUserReservation = Reservation.builder()
                .userId(2L)
                .scheduleId(schedule.getId())
                .quantity(3)
                .status(ReservationStatus.PAYMENTS_CONFIRMED)
                .build();
        otherUserReservation = reservationRepository.save(otherUserReservation);
        log.info("다른 사용자의 예약 저장 완료: id={}", otherUserReservation.getId());

        // 다른 사용자의 환불 저장
        refundService.save(otherUserReservation);
        log.info("다른 사용자의 환불 저장 완료");

        // when
        Page<RefundDetailResponse> userRefundDetails = refundService.findAllRefundsDetailByUserId(userId, PageRequest.of(0, 10));
        log.info("user{}의 환불 상세 정보 조회 완료: size={}", userId, userRefundDetails.getTotalElements());

        // then
        assertThat(userRefundDetails.getContent()).hasSize(2);

        // 첫 번째 환불 상세 정보 검증
        RefundDetailResponse firstRefundDetail = userRefundDetails.getContent().get(0);
        RefundTestUtils.logRefundDetailResponse(firstRefundDetail, "첫 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(firstRefundDetail, reservation, schedule, performance);

        // 두 번째 환불 상세 정보 검증
        RefundDetailResponse secondRefundDetail = userRefundDetails.getContent().get(1);
        RefundTestUtils.logRefundDetailResponse(secondRefundDetail, "두 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(secondRefundDetail, reservation2, schedule, performance);

        log.info("특정 사용자의 환불 상세 정보 조회 테스트 완료");
    }

    @Test
    @DisplayName("환불 승인 테스트")
    void confirmRefundTest() throws Exception {
        // given
        log.info("환불 승인 테스트 시작");

        // 환불 저장
        refundService.save(reservation);
        Refund savedRefund = refundRepository.findRefundByReservationId(reservation.getId()).orElseThrow();
        log.info("환불 저장 완료: refundId={}", savedRefund.getId());

        // when
        refundService.confirmRefund(savedRefund.getId());
        log.info("환불 승인 완료");

        // then
        Refund confirmedRefund = refundRepository.findById(savedRefund.getId()).orElseThrow();

        // 환불 상태가 CONFIRMED로 변경되었는지 확인
        assertThat(confirmedRefund.getStatus()).isEqualTo(RefundStatus.CONFIRMED);
        log.info("환불 상태가 CONFIRMED로 변경됨");

        log.info("환불 승인 테스트 완료");
    }

    /*------------- 실패 테스트 ------------*/

    @Test
    @DisplayName("존재하지 않는 환불 ID로 승인 시도시 예외 발생")
    void confirmRefundWithInvalidIdTest() {
        // given
        log.info("존재하지 않는 환불 ID로 승인 시도 테스트 시작");
        Long invalidId = 999L;

        // when & then
        assertThatThrownBy(() -> refundService.confirmRefund(invalidId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFUND_NOT_FOUND);

        log.info("존재하지 않는 환불 ID로 승인 시도 테스트 완료");
    }

    @Test
    @DisplayName("이미 존재하는 예약 ID로 환불 요청 시도시 예외 발생")
    void saveWithDuplicateReservationIdTest() {
        // given
        log.info("중복 예약 ID 테스트 시작");

        // 첫 번째 환불 요청 저장
        refundService.save(reservation);
        log.info("첫 번째 환불 요청 저장 완료");

        // when & then
        assertThatThrownBy(() -> refundService.save(reservation))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_REFUND);

        log.info("중복 예약 ID 테스트 완료");
    }

    @Test
    @DisplayName("존재하지 않는 환불 ID로 상세 정보 조회 시 예외 발생")
    void findRefundsDetailByInvalidRefundIdTest() {
        // given
        log.info("존재하지 않는 환불 ID 테스트 시작");
        Long invalidRefundId = 999L;

        // when & then
        assertThatThrownBy(() -> refundService.findRefundsDetailByRefundId(invalidRefundId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFUND_NOT_FOUND);

        log.info("존재하지 않는 환불 ID 테스트 완료");
    }
}