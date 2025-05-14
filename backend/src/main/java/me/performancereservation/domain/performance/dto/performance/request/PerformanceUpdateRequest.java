package me.performancereservation.domain.performance.dto.performance.request;

import jakarta.validation.constraints.NotNull;

public record PerformanceUpdateRequest(
        @NotNull Long fileId,
        @NotNull String description) {}
