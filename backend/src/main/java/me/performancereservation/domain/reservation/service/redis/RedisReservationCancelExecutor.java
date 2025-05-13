package me.performancereservation.domain.reservation.service.redis;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.refund.RefundService;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.global.storage.redis.RedisReservationService;
import me.performancereservation.global.storage.redis.RedisSeatService;
import org.springframework.stereotype.Component;

/**
 * 예약 취소 로직 실행기
 */
@Component
@RequiredArgsConstructor
public class RedisReservationCancelExecutor {

    private final RefundService refundService;
    private final RedisSeatService redisSeatService;
    private final RedisReservationService redisReservationService;

    // 예약 취소 로직 실행 (단일 예약 취소)
    public void execute(Reservation reservation) {
        // 결제 완료된 예약인 경우 환불 요청 상태로 변경
        if (reservation.isRefundRequired()) {
            reservation.requestCancel(); // CANCEL_PENDING

            // 환불 객체 생성
            refundService.save(reservation);
        } else {
            // 결제 미완료 상태라면 바로 취소 확정 처리
            reservation.cancelConfirm(); // CANCEL_CONFIRMED
        }

        // Redis 좌석 롤백 처리
        redisSeatService.safeIncrement(reservation.getScheduleId(), reservation.getQuantity());

        // 예약 만료 처리 대기 큐에서도 제거
        redisReservationService.removeFromPendingExpirationQueue(reservation.getId());
    }
}