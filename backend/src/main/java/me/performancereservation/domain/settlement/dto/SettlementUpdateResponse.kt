package me.performancereservation.domain.settlement.dto;

import jakarta.validation.constraints.NotNull;
import me.performancereservation.domain.settlement.Settlement;

public record SettlementUpdateResponse (
        Long settlementId,
        Long performanceId,
        String bank,
        String account
) {
    public static SettlementUpdateResponse fromSettlement(Settlement settlement) {
        return new SettlementUpdateResponse(
                settlement.getId(),
                settlement.getPerformanceId(),
                settlement.getBank(),
                settlement.getAccount()
        );
    }
}
