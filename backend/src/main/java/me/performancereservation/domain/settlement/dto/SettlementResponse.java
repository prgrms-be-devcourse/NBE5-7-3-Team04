package me.performancereservation.domain.settlement.dto;

import me.performancereservation.domain.settlement.Settlement;
import me.performancereservation.domain.settlement.enums.SettlementStatus;

import java.time.LocalDateTime;

public record SettlementResponse(
        // settlement 내용에서 가져옴
        Long settlementId, // PK
        Integer totalAmount, // 계산
        LocalDateTime settledAt, // 처음에 null
        String account,
        String bank,
        SettlementStatus status,

// performance 내용에서 가져옴
        String title
) {
    public static SettlementResponse fromEntity(Settlement settlement, String title) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getTotalAmount(),
                settlement.getSettledAt(),
                settlement.getAccount(),
                settlement.getBank(),
                settlement.getStatus(),
                title
        );
    }
}
