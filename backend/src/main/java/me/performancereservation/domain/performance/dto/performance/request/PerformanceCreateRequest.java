package me.performancereservation.domain.performance.dto.performance.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record PerformanceCreateRequest(
        @NotNull String title,
        @NotNull String venue,
        @Positive int price,
        @Positive int totalSeats,
        @NotNull String category,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        @NotNull String description,
        Long fileId) {
}
