package me.performancereservation.domain.performance.dto.performanceschedule;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PerformanceScheduleRequest(
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime) {
}
