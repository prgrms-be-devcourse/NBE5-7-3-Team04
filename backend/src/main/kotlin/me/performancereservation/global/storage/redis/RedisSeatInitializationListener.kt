package me.performancereservation.global.storage.redis

import me.performancereservation.domain.performance.event.PerformanceScheduleCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class RedisSeatInitializationListener(
    private val redisSeatService: RedisSeatService
) {
    @EventListener
    fun handlePerformanceScheduleCreated(event: PerformanceScheduleCreatedEvent) {
        redisSeatService.initializeSeatStock(
            scheduleId = event.scheduleId,
            stock = event.totalSeats
        )
    }
}
