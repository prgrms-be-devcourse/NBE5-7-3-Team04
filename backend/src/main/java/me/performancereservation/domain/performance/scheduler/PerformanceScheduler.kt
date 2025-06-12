package me.performancereservation.domain.performance.scheduler

import me.performancereservation.domain.performance.service.PerformanceService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PerformanceScheduler (
    private val performanceService: PerformanceService
) {

    @Scheduled(cron = "0 0 0 * * *")
    fun completeEndedPerformances() {
        performanceService.completeEndedPerformances()
    }
}
