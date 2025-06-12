package me.performancereservation.domain.refund.dto

import jakarta.validation.constraints.NotNull

data class UpdateBankInfoRequest(
    @field: NotNull
    val refundId: Long,  // 정보 추가할 환불id
    @field: NotNull
    val account: String,  // 환불 받을 계좌번호
    @field: NotNull
    val bank: String,  // 환불 받을 은행
    @field: NotNull
    val depositorName: String // 입금자명
)
