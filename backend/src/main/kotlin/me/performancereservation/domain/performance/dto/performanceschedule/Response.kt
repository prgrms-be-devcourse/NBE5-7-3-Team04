package me.performancereservation.domain.performance.dto.performanceschedule

import java.time.LocalDateTime

data class PerformanceScheduleResponse(
    val id: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val remainingSeats: Int,
    val isCanceled: Boolean
)