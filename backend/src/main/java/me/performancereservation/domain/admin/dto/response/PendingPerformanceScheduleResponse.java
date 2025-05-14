package me.performancereservation.domain.admin.dto.response;

import java.time.LocalDateTime;

public record PendingPerformanceScheduleResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
