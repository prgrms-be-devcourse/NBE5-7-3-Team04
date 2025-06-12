package me.performancereservation.domain.settlement.dto

import me.performancereservation.domain.settlement.Settlement
import me.performancereservation.domain.settlement.enums.SettlementStatus
import java.time.LocalDateTime

data class SettlementResponse( // settlement 내용에서 가져옴
    val settlementId: Long,  // PK
    val totalAmount: Int,  // 계산

    val settledAt: LocalDateTime?,  // 처음에 null

    val account: String,
    val bank: String,
    val status: SettlementStatus,  // performance 내용에서 가져옴

    val title: String
) {
    companion object {
        @JvmStatic
        fun fromEntity(settlement: Settlement, title: String): SettlementResponse {
            return SettlementResponse(
                settlement.id!!,
                settlement.totalAmount,
                settlement.settledAt,
                settlement.account,
                settlement.bank,
                settlement.status,
                title
            )
        }
    }
}
