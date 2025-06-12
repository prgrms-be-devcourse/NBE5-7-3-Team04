package me.performancereservation.domain.performance.dto.performanceschedule

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class PerformanceScheduleRequest(
    @field:NotNull val startTime: LocalDateTime,
    @field:NotNull val endTime: LocalDateTime
)
