package me.performancereservation.domain.performance.event;

public record PerformanceScheduleCreatedEvent(Long scheduleId, int totalSeats) {
}
