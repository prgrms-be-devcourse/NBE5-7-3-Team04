package me.performancereservation.domain.performance.dto.performanceschedule;

import java.time.LocalDateTime;

public record PerformanceScheduleResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int remainingSeats,
        Boolean isCanceled
        ) {
}
