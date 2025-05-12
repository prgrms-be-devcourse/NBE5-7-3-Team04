package me.performancereservation.domain.refund;

import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.refund.dto.RefundRequest;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.dto.RefundResponse;
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

    private RefundRequest refundRequest;
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

        // RefundRequest 생성
        refundRequest = new RefundRequest(
                reservation.getId(),
                1L,
                "123-456-789",
                "신한은행"
        );
        log.info("첫 번째 환불요청 : {}", refundRequest);

        refund = Refund.builder()
                .id(1L)
                .reservationId(reservation.getId())
                .userId(1L)
                .account("123-456-789")
                .bank("신한은행")
                .status(RefundStatus.PENDING)
                .build();
        log.info("테스트 설정 완료: refundRequest={}", refundRequest);
    }

    @Test
    @DisplayName("save 테스트")
    void saveTest() throws Exception {
        // given
        log.info("환불 저장 테스트 시작");
        RefundTestUtils.logRefundRequest(refundRequest, "저장할 환불 요청 정보");

        // when
        RefundResponse savedRefundResponse = refundService.save(refundRequest);
        log.info("환불 저장 완료: savedId={}", savedRefundResponse.refundId());

        // then
        Refund savedRefund = refundRepository.findById(savedRefundResponse.refundId()).orElseThrow();
        RefundTestUtils.logRefundEntity(savedRefund, "DB에서 조회한 환불 정보");

        assertThat(savedRefundResponse.refundId()).isNotNull();
        assertThat(savedRefundResponse.reservationId()).isEqualTo(refundRequest.reservationId());
        assertThat(savedRefundResponse.account()).isEqualTo(refundRequest.account());
        assertThat(savedRefundResponse.bank()).isEqualTo(refundRequest.bank());
        assertThat(savedRefundResponse.status()).isEqualTo(RefundStatus.PENDING);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCEL_PENDING);
        log.info("PAYMENTS_CONFIRMED인 경우 PENDING, CANCEL_PENDING으로 환불 생성");
        log.info("환불 저장 테스트 완료");
    }

    @Test
    @DisplayName("PAYMENTS_PENDING 상태의 예약에 대한 환불 저장 테스트")
    void saveWithPaymentsPendingTest() throws Exception {
        // given
        log.info("PAYMENTS_PENDING 상태의 예약에 대한 환불 저장 테스트 시작");
        
        // 예약 상태를 PAYMENTS_PENDING으로 변경
        reservation.setStatus(ReservationStatus.PAYMENTS_PENDING);
        RefundTestUtils.logRefundRequest(refundRequest, "저장할 환불 요청 정보");

        // when
        RefundResponse savedRefundResponse = refundService.save(refundRequest);
        log.info("환불 저장 완료: response={}", savedRefundResponse);

        // then
        assertThat(savedRefundResponse).isNull();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCEL_CONFIRMED);
        log.info("PAYMENTS_PENDING인 경우 환불 생성 없이 예약만 CANCEL_CONFIRMED로 변경");
        log.info("PAYMENTS_PENDING 상태의 예약에 대한 환불 저장 테스트 완료");
    }

    @Test
    @DisplayName("전체 환불 상세 정보 조회 테스트")
    void findAllRefundsDetailTest() throws Exception {
        // given
        log.info("환불 상세 정보 조회 테스트 시작");

        // 첫 번째 환불 저장
        RefundResponse saved1 = refundService.save(refundRequest);
        log.info("첫 번째 환불 저장 완료: refundId={}", saved1.refundId());

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
        RefundRequest refundRequest2 = new RefundRequest(
            reservation2.getId(),
            2L,
            "987-654-321",
            "국민은행"
        );
        RefundResponse saved2 = refundService.save(refundRequest2);
        log.info("두 번째 환불 저장 완료: {}", saved2);

        // when
        Page<RefundDetailResponse> refundDetailResponses = refundService.findAllRefundsDetail(PageRequest.of(0,10));
        log.info("환불 상세 정보 조회 완료: size={}", refundDetailResponses.getTotalElements());

        // then
        assertThat(refundDetailResponses.getTotalElements()).isEqualTo(2);

        // 첫 번째 환불 상세 정보 검증
        RefundDetailResponse firstRefundDetail = refundDetailResponses.getContent().get(1);
        RefundTestUtils.logRefundDetailResponse(firstRefundDetail, "첫 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(firstRefundDetail, refundRequest, reservation, schedule, performance);

        // 두 번째 환불 상세 정보 검증
        RefundDetailResponse secondRefundDetail = refundDetailResponses.getContent().get(0);
        RefundTestUtils.logRefundDetailResponse(secondRefundDetail, "두 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(secondRefundDetail, refundRequest2, reservation2, schedule, performance);

        log.info("환불 상세 정보 조회 테스트 완료");
    }

    @Test
    @DisplayName("특정 사용자의 환불 상세 정보 조회 테스트")
    void    findAllRefundsDetailByUserIdTest() throws Exception {
        // given
        log.info("특정 사용자의 환불 상세 정보 조회 테스트 시작");
        Long userId = 1L;

        // 첫 번째 환불 저장
        RefundResponse saved1 = refundService.save(refundRequest);
        log.info("userid {}의 첫 번째 환불 저장 완료: refundId={}", userId, saved1.refundId());

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
        RefundRequest refundRequest2 = new RefundRequest(
            reservation2.getId(),
            1L,
            "987-654-321",
            "국민은행"
        );
        RefundResponse saved2 = refundService.save(refundRequest2);
        log.info("두 번째 환불 저장 완료: refundId={} userid{}", saved2.refundId(), saved2.userId());
        log.info("saved2 = {}", saved2);

        // 다른 사용자의 예약 생성
        Reservation otherUserReservation = Reservation.builder()
                .userId(2L)
                .scheduleId(schedule.getId())
                .quantity(3)
                .status(ReservationStatus.PAYMENTS_CONFIRMED)
                .build();
        otherUserReservation = reservationRepository.save(otherUserReservation);
        log.info("다른 사용자의 예약 저장 완료: id={}", otherUserReservation.getId());
        log.info("otherUserReservation = {}", otherUserReservation);

        // 다른 사용자의 환불 저장
        RefundRequest otherUserRefundRequest = new RefundRequest(
            otherUserReservation.getId(),
            2L,
            "111-222-333",
            "우리은행"
        );
        RefundResponse otherUserSaved = refundService.save(otherUserRefundRequest);
        log.info("다른 사용자의 환불 저장 완료: refundId={}", otherUserSaved.refundId());
        log.info("otherUserSaved = {}", otherUserSaved);

        // when
        Page<RefundDetailResponse> userRefundDetails = refundService.findAllRefundsDetailByUserId(userId, PageRequest.of(0, 10));
        log.info("user{}의 환불 상세 정보 조회 완료: size={}", userId, userRefundDetails.getTotalElements());

        log.info("userRefundDetails.getTotalElements() = {}", userRefundDetails.getTotalElements());
        log.info("userRefundDetails.getContent() = {}", userRefundDetails.getContent());
        // then
//        assertThat(userRefundDetails.getTotalElements()).isEqualTo(2);
        assertThat(userRefundDetails.getContent()).hasSize(2);

        // 첫 번째 환불 상세 정보 검증
        RefundDetailResponse firstRefundDetail = userRefundDetails.getContent().get(0);
        RefundTestUtils.logRefundDetailResponse(firstRefundDetail, "첫 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(firstRefundDetail, refundRequest, reservation, schedule, performance);

        // 두 번째 환불 상세 정보 검증
        RefundDetailResponse secondRefundDetail = userRefundDetails.getContent().get(1);
        RefundTestUtils.logRefundDetailResponse(secondRefundDetail, "두 번째 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(secondRefundDetail, refundRequest2, reservation2, schedule, performance);

        log.info("특정 사용자의 환불 상세 정보 조회 테스트 완료");
    }

    @Test
    @DisplayName("환불 상태별 상세 정보 조회 테스트")
    void findAllRefundsDetailByRefundStatusTest() throws Exception {
        // given
        log.info("환불 상태별 상세 정보 조회 테스트 시작");

        // 첫 번째 PENDING 상태의 환불 저장
        RefundResponse saved1 = refundService.save(refundRequest);
        log.info("첫 번째 PENDING 상태 환불 저장 완료: refundId={}", saved1.refundId());

        // 두 번째 예약 생성
        Reservation reservation2 = Reservation.builder()
                .userId(2L)
                .scheduleId(schedule.getId())
                .quantity(1)
                .status(ReservationStatus.PAYMENTS_CONFIRMED)
                .build();
        reservation2 = reservationRepository.save(reservation2);
        log.info("두 번째 예약 저장 완료: id={}", reservation2.getId());

        // 세 번째 예약 생성 (user 2, PAYMENTS_PENDING 상태)
        Reservation reservation3 = Reservation.builder()
                .userId(2L)
                .scheduleId(schedule.getId())
                .quantity(3)
                .status(ReservationStatus.PAYMENTS_PENDING)
                .build();
        reservation3 = reservationRepository.save(reservation3);
        log.info("세 번째 예약 저장 완료: id={}", reservation3.getId());

        // 두 번째 PENDING 상태의 환불 저장
        RefundRequest refundRequest2 = new RefundRequest(
            reservation2.getId(),
            2L,
            "987-654-321",
            "국민은행"
        );
        RefundResponse saved2 = refundService.save(refundRequest2);
        log.info("두 번째 PENDING 상태 환불 저장 완료: refundId={}", saved2.refundId());

        // PAYMENTS_PENDING 상태의 예약에 대한 환불 요청 (환불 생성되지 않음)
        RefundRequest pendingRequest = new RefundRequest(
            reservation3.getId(),
            2L,
            "111-222-333",
            "우리은행"
        );
        RefundResponse pendingResponse = refundService.save(pendingRequest);
        log.info("PAYMENTS_PENDING 상태 예약의 환불 요청 완료: response={}", pendingResponse);

        // when
        Page<RefundDetailResponse> pendingRefunds = refundService.findAllRefundsDetailByRefundStatus("pending", PageRequest.of(0, 10));
        Page<RefundDetailResponse> confirmedRefunds = refundService.findAllRefundsDetailByRefundStatus("confirmed", PageRequest.of(0, 10));

        log.info("PENDING 상태 환불 상세 정보 조회 완료: size={}", pendingRefunds.getTotalElements());
        log.info("CONFIRMED 상태 환불 상세 정보 조회 완료: size={}", confirmedRefunds.getTotalElements());

        // then
        // PENDING 상태 환불 검증
        assertThat(pendingRefunds.getTotalElements()).isEqualTo(2);

        // 첫 번째 PENDING 환불 검증
        RefundDetailResponse pendingRefund1 = pendingRefunds.getContent().get(0);
        RefundTestUtils.logRefundDetailResponse(pendingRefund1, "첫 번째 PENDING 상태 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(pendingRefund1, refundRequest, reservation, schedule, performance);

        // 두 번째 PENDING 환불 검증
        RefundDetailResponse pendingRefund2 = pendingRefunds.getContent().get(1);
        RefundTestUtils.logRefundDetailResponse(pendingRefund2, "두 번째 PENDING 상태 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(pendingRefund2, refundRequest2, reservation2, schedule, performance);

        // CONFIRMED 상태 환불 검증 (PAYMENTS_PENDING은 환불이 생성되지 않으므로 0개)
        assertThat(confirmedRefunds.getTotalElements()).isEqualTo(0);

        log.info("환불 상태별 상세 정보 조회 테스트 완료");
    }

    @Test
    @DisplayName("특정 환불 ID로 상세 정보 조회 테스트")
    void findRefundsDetailByRefundIdTest() throws Exception {
        // given
        log.info("특정 환불 ID로 상세 정보 조회 테스트 시작");

        // 환불 저장
        RefundResponse savedRefund = refundService.save(refundRequest);
        log.info("환불 저장 완료: refundId={}", savedRefund.refundId());

        // when
        RefundDetailResponse refundDetail = refundService.findRefundsDetailByRefundId(savedRefund.refundId());
        log.info("환불 상세 정보 조회 완료");

        // then
        RefundTestUtils.logRefundDetailResponse(refundDetail, "조회된 환불 상세 정보");
        RefundTestUtils.assertRefundDetailResponse(refundDetail, refundRequest, reservation, schedule, performance);
        log.info("특정 환불 ID로 상세 정보 조회 테스트 완료");
    }

    @Test
    @DisplayName("환불 승인 테스트")
    void confirmRefundTest() throws Exception {
        // given
        log.info("환불 승인 테스트 시작");

        // 환불 저장
        RefundResponse savedRefund = refundService.save(refundRequest);
        log.info("환불 저장 완료: refundId={}", savedRefund.refundId());

        // when
        refundService.confirmRefund(savedRefund.refundId());
        log.info("환불 승인 완료");

        // then
        Refund confirmedRefund = refundRepository.findById(savedRefund.refundId()).orElseThrow();
        Reservation confirmedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();

        // 환불 상태가 CONFIRMED로 변경되었는지 확인
        assertThat(confirmedRefund.getStatus()).isEqualTo(RefundStatus.CONFIRMED);
        log.info("환불 상태가 CONFIRMED로 변경됨");

        // 예약 상태가 CANCEL_CONFIRMED로 변경되었는지 확인
        assertThat(confirmedReservation.getStatus()).isEqualTo(ReservationStatus.CANCEL_CONFIRMED);
        log.info("예약 상태가 CANCEL_CONFIRMED로 변경됨");

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
    @DisplayName("이미 승인된 환불을 다시 승인 시도시 예외 발생")
    void confirmAlreadyConfirmedRefundTest() {
        // given
        log.info("이미 승인된 환불 재승인 시도 테스트 시작");

        // 환불 저장
        RefundResponse savedRefund = refundService.save(refundRequest);
        log.info("환불 저장 완료: refundId={}", savedRefund.refundId());

        // 첫 번째 승인
        refundService.confirmRefund(savedRefund.refundId());
        log.info("첫 번째 승인 완료");

        // when & then
        assertThatThrownBy(() -> refundService.confirmRefund(savedRefund.refundId()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFUND_STATUS);

        log.info("이미 승인된 환불 재승인 시도 테스트 완료");
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

    @Test
    @DisplayName("존재하지 않는 환불 ID로 상태 변경 시도시 예외 발생")
    void updateRefundStatusWithInvalidIdTest() {
        // given
        log.info("존재하지 않는 환불 ID 테스트 시작");
        Long invalidId = 999L;
        RefundStatus newStatus = RefundStatus.CONFIRMED;

        /* Service 측에서 AppException(ErrorCode.NO_SUCH_REFUND_ERROR, ErrorType.DOMAIN))을 던진다
            updateRefundStatus에서 던져진 예외가 AppException 클래스가 아니거나
            errorCode 필드의 내용이 아래와 같지 않으면 assert */
        assertThatThrownBy(() -> refundService.updateRefundStatus(invalidId, newStatus))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFUND_NOT_FOUND);

        log.info("존재하지 않는 환불 ID 테스트 완료");
    }

    @Test
    @DisplayName("이미 존재하는 예약 ID로 환불 요청 시도시 예외 발생")
    void saveWithDuplicateReservationIdTest() {
        // given
        log.info("중복 예약 ID 테스트 시작");

        // 첫 번째 환불 요청 저장
        RefundResponse saved = refundService.save(refundRequest);
        log.info("첫 번째 환불 요청 저장 완료: refundId={}", saved.refundId());

        // 같은 예약 ID로 두 번째 환불 요청 생성
        RefundRequest duplicateRequest = new RefundRequest(
            refundRequest.reservationId(), // 동일한 예약 ID
            2L, // 다른 사용자 ID
            "987-654-321",
            "국민은행"
        );

        // when & then
        assertThatThrownBy(() -> refundService.save(duplicateRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_REFUND);

        log.info("중복 예약 ID 테스트 완료");
    }

}