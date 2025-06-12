package me.performancereservation.domain.settlement.dto

import me.performancereservation.domain.settlement.Settlement

data class SettlementUpdateResponse(
    val settlementId: Long,
    val performanceId: Long,
    val bank: String,
    val account: String
) {
    companion object {
        @JvmStatic
        fun fromSettlement(settlement: Settlement): SettlementUpdateResponse {
            return SettlementUpdateResponse(
                settlement.id!!,
                settlement.performanceId,
                settlement.bank,
                settlement.account
            )
        }
    }
}
