package me.performancereservation.domain.refund.dto

import jakarta.validation.constraints.NotNull

@JvmRecord
data class UpdateBankInfoRequest(
    @JvmField val refundId: @NotNull Long?,  // 정보 추가할 환불id
    @JvmField val account: @NotNull String?,  // 환불 받을 계좌번호
    @JvmField val bank: @NotNull String?,  // 환불 받을 은행
    @JvmField val depositorName: @NotNull String? // 입금자명
)
