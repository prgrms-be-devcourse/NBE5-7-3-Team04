package me.performancereservation.domain.performance.dto.performance.event;

public record PerformanceScheduleCreatedEvent(Long scheduleId, int totalSeats) {
}
