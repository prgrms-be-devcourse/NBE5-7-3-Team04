package me.performancereservation.domain.reservation.service

import me.performancereservation.domain.performance.event.PerformanceCanceledEvent
import me.performancereservation.domain.performance.event.ScheduleCanceledEvent
import me.performancereservation.domain.reservation.service.redis.RedisReservationBulkCancelService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ReservationEventListener(
    private val reservationBulkCancelService: RedisReservationBulkCancelService
) {
    /**
     * 공연이 취소됐을 때 발생하는 이벤트를 핸들링하는 리스너
     * 취소된 공연에 대한 예약들을 백그라운드에서 일괄 취소 처리함
     *
     * @param event 공연취소 이벤트 (performanceId)
     */
    @Async
    @EventListener
    fun handlePerformanceCanceled(event: PerformanceCanceledEvent) {
        reservationBulkCancelService.cancelAllByPerformanceId(event.performanceId)
    }

    /**
     * 공연 회차가 취소됐을 때 발생하는 이벤트를 핸들링하는 리스너
     * 취소된 공연회차에 대한 예약들을 백그라운드에서 일괄 취소 처리함
     *
     * @param event 공연 회차 취소 이벤트 (scheduleId)
     */
    @Async
    @EventListener
    fun handleScheduleCanceled(event: ScheduleCanceledEvent) {
        reservationBulkCancelService.cancelAllByScheduleId(event.scheduleId)
    }
}
