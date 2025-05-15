package me.performancereservation.domain.settlement.dto;

import jakarta.validation.constraints.NotNull;

public record SettlementRequest(
    @NotNull long performanceId,
    @NotNull String account,
    @NotNull String bank
) {
}
