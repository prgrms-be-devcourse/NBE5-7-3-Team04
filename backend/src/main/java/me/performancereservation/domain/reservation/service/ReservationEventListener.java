package me.performancereservation.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.event.PerformanceCanceledEvent;
import me.performancereservation.domain.reservation.service.redis.RedisReservationBulkCancelService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final RedisReservationBulkCancelService reservationBulkCancelService;

    /**
     * 공연이 취소됐을 때 발생하는 이벤트를 핸들링하는 리스너
     * 취소된 공연에 대한 예약들을 백그라운드에서 일괄 취소 처리함
     *
     * @param event 공연취소 이벤트 (performanceId)
     */
    @Async
    @EventListener
    public void handlePerformanceCanceled(PerformanceCanceledEvent event) {
        reservationBulkCancelService.cancelAllByPerformanceId(event.performanceId());
    }
}
