package me.performancereservation.domain.settlement.dto;

import jakarta.validation.constraints.NotNull;

public record SettlementUpdateRequest(
        @NotNull Long settlementId,
        @NotNull String bank,
        @NotNull String account
) {
}
