package me.performancereservation.domain.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.global.storage.redis.RedisReservationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpirationScheduler {
    private final ReservationRepository reservationRepository;
    private final RedisReservationService redisReservationService;

    @Scheduled(fixedDelay = 10000)
    public void cancelExpiredReservations() {
        var expiredIds = redisReservationService.findExpiredPendingReservations();

        for (String reservationIdStr : expiredIds) {
            try {
                Long reservationId = Long.parseLong(reservationIdStr);

                reservationRepository.findById(reservationId).ifPresent(reservation -> {
                    if (reservation.isCancelable()) {
                        reservation.cancelConfirm(); // 도메인 상태 변경

                        log.info(reservationId + " 의 상태 변경: CANCEL_CONFIRMED");
                    }

                    // Redis에서 큐에서 제거
                    redisReservationService.removeFromPendingExpirationQueue(reservationId);
                });

            } catch (Exception e) {
                log.error("[예약 만료 처리 실패] reservationId={} 처리 중 에러 발생", reservationIdStr, e);
            }
        }
    }
}