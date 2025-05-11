package me.performancereservation.domain.refund;

import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.refund.dto.RefundRequest;
import me.performancereservation.domain.refund.dto.RefundResponse;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.reservation.Reservation;
import org.assertj.core.api.Assertions;
import java.time.temporal.ChronoUnit;

@Slf4j
public class RefundTestUtils {

    public static void logRefundRequest(RefundRequest request, String prefix) {
        log.info("{}: reservationId={}, userId={}, account={}, bank={}",
                prefix,
                request.reservationId(),
                request.userId(),
                request.account(),
                request.bank());
    }

    public static void logRefundEntity(Refund refund, String prefix) {
        log.info("{}: id={}, reservationId={}, userId={}, account={}, bank={}, status={}, createdAt={}, updatedAt={}",
                prefix,
                refund.getId(),
                refund.getReservationId(),
                refund.getUserId(),
                refund.getAccount(),
                refund.getBank(),
                refund.getStatus(),
                refund.getCreatedAt(),
                refund.getUpdatedAt());
    }

    public static void logRefundResponse(RefundResponse response, String prefix) {
        log.info("{}: refundId={}, reservationId={}, account={}, bank={}, status={}",
                prefix,
                response.refundId(),
                response.reservationId(),
                response.account(),
                response.bank(),
                response.status());
    }

    public static void logRefundDetailResponse(RefundDetailResponse response, String message) {
        log.info("=== {} ===", message);
        log.info("환불 ID: {}", response.refundId());
        log.info("예약 ID: {}", response.reservationId());
        log.info("계좌번호: {}", response.account());
        log.info("은행: {}", response.bank());
        log.info("환불 상태: {}", response.refundStatus());
        log.info("예약 수량: {}", response.quantity());
        log.info("공연 시작 시간: {}", response.startTime());
        log.info("공연 제목: {}", response.title());
        log.info("공연 장소: {}", response.venue());
        log.info("공연 가격: {}", response.price());
        log.info("공연 분류: {}", response.category());
        log.info("공연 일시: {}", response.performance_date());
        log.info("공연 설명: {}", response.description());
        log.info("==================");
    }

    public static void assertRefundDetailResponse(RefundDetailResponse response, RefundRequest request, Reservation reservation, PerformanceSchedule schedule, Performance performance) {
        Assertions.assertThat(response.refundId()).isNotNull();
        Assertions.assertThat(response.reservationId()).isEqualTo(request.reservationId());
        Assertions.assertThat(response.userId()).isEqualTo(request.userId());
        Assertions.assertThat(response.account()).isEqualTo(request.account());
        Assertions.assertThat(response.bank()).isEqualTo(request.bank());
        Assertions.assertThat(response.quantity()).isEqualTo(reservation.getQuantity());
        Assertions.assertThat(response.startTime().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(schedule.getStartTime().truncatedTo(ChronoUnit.SECONDS));
        Assertions.assertThat(response.fileId()).isEqualTo(performance.getFileId());
        Assertions.assertThat(response.title()).isEqualTo(performance.getTitle());
        Assertions.assertThat(response.venue()).isEqualTo(performance.getVenue());
        Assertions.assertThat(response.price()).isEqualTo(performance.getPrice());
        Assertions.assertThat(response.category()).isEqualTo(performance.getCategory().toString());
        Assertions.assertThat(response.performance_date()).isEqualTo(performance.getPerformance_date());
        Assertions.assertThat(response.description()).isEqualTo(performance.getDescription());
    }
}
