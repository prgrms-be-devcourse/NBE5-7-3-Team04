package me.performancereservation.domain.settlement.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SettlementRequest(
    @field: NotNull(message = "정산할 공연 id가 반드시 필요합니다")
    val performanceId: Long,

    @field: NotBlank(message = "계좌번호가 반드시 필요합니다")
    val account: String,

    @field: NotBlank(message = "은행명이 반드시 필요합니다")
    val bank: String
)
