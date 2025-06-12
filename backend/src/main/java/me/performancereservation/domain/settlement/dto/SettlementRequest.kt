package me.performancereservation.domain.settlement.dto;

import jakarta.validation.constraints.NotNull;

public record SettlementRequest(
    @NotNull Long performanceId,
    @NotNull String account,
    @NotNull String bank
) {
}
