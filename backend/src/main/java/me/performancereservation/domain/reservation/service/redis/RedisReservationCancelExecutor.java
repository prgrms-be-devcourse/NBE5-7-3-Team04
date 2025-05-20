package me.performancereservation.domain.reservation.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.refund.RefundService;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.global.storage.redis.RedisReservationService;
import me.performancereservation.global.storage.redis.RedisSeatService;
import org.springframework.stereotype.Component;

/**
 * 예약 취소 로직 실행기
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisReservationCancelExecutor {

    private final RefundService refundService;
    private final RedisSeatService redisSeatService;
    private final RedisReservationService redisReservationService;

    private final PerformanceScheduleRepository performanceScheduleRepository;

    // 유저가 취소했을 경우 (레디스 좌석 롤백 필요)
    public void executeForUserCancel(Reservation reservation) {
        execute(reservation, true);
    }

    // 공연이 취소됐을 경우 (레디스 좌석 롤백 불필요)
    public void executeForPerformanceCancel(Reservation reservation) {
        execute(reservation, false);
    }

    // 예약 취소 로직 실행 (단일 예약 취소 + 환불 객체 생성)
    private void execute(Reservation reservation, boolean rollbackSeat) {
        log.info("예약 취소 시작: 예약 ID = {}, 유저 ID = {}, 공연 회차 ID = {}, 수량 = {}, 결제 상태 = {}",
                reservation.getId(), reservation.getUserId(), reservation.getScheduleId(),
                reservation.getQuantity(), reservation.getStatus());

                // 결제 완료된 예약인 경우 환불 요청 상태로 변경
        if (reservation.isRefundRequired()) {
            reservation.requestCancel(); // CANCEL_PENDING

            // 티켓 수 만큼 공연 회차의 좌석수를 롤백해서 레디스와의 데이터 정합성 맞춰주기
            performanceScheduleRepository.findById(reservation.getScheduleId())
                    .ifPresent(schedule -> schedule.increaseRemainingSeats(reservation.getQuantity()));

            // 환불 객체 생성
            refundService.save(reservation);
        } else {
            // 결제 미완료 상태라면 바로 취소 확정 처리
            reservation.cancelConfirm(); // CANCEL_CONFIRMED
        }

        // 레디스 좌석 롤백이 필요한가?
        if (rollbackSeat) {
            // Redis 좌석 롤백 처리
            redisSeatService.safeIncrement(reservation.getScheduleId(), reservation.getQuantity());
        }

        // 예약 만료 처리 대기 큐에서도 제거
        redisReservationService.removeFromPendingExpirationQueue(reservation.getId());
    }
}