//package me.performancereservation.domain.refund;
//
//import lombok.extern.slf4j.Slf4j;
//import me.performancereservation.domain.performance.entities.Performance;
//import me.performancereservation.domain.performance.entities.PerformanceSchedule;
//import me.performancereservation.domain.refund.dto.RefundResponse;
//import me.performancereservation.domain.refund.dto.RefundDetailResponse;
//import me.performancereservation.domain.reservation.Reservation;
//import java.time.temporal.ChronoUnit;
//
//@Slf4j
//public class RefundTestUtils {
//
//    public static void logRefundEntity(Refund refund, String message) {
//        log.info("{}: id={}, reservationId={}, userId={}, status={}",
//                message,
//                refund.getId(),
//                refund.getReservationId(),
//                refund.getUserId(),
//                refund.getStatus());
//    }
//
//    public static void logRefundResponse(RefundResponse response, String prefix) {
//        log.info("{}: refundId={}, reservationId={}, account={}, bank={}, status={}",
//                prefix,
//                response.refundId,
//                response.reservationId,
//                response.account,
//                response.bank,
//                response.status);
//    }
//
//    public static void logRefundDetailResponse(RefundDetailResponse response, String message) {
//        log.info("{}: refundId={}, reservationId={}, userId={}, status={}, quantity={}, startTime={}, performanceTitle={}",
//                message,
//                response.refundId,
//                response.reservationId,
//                response.userId,
//                response.refundStatus,
//                response.quantity,
//                response.startTime,
//                response.title);
//    }
//
//    public static void assertRefundDetailResponse(RefundDetailResponse response, Reservation reservation, PerformanceSchedule schedule, Performance performance) {
//        assert response.quantity.equals(reservation.getQuantity());
//        assert response.startTime.truncatedTo(ChronoUnit.SECONDS)
//                .equals(schedule.getStartTime().truncatedTo(ChronoUnit.SECONDS));
//        assert response.title.equals(performance.getTitle());
//    }
//}
