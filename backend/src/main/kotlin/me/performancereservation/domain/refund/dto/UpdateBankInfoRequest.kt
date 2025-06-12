package me.performancereservation.domain.refund.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateBankInfoRequest(
    @field: NotNull
    val refundId: Long,  // 정보 추가할 환불id

    @field: NotBlank(message = "계좌번호는 반드시 입력되어야 합니다")
    val account: String,  // 환불 받을 계좌번호

    @field: NotBlank(message = "은행명은 반드시 입력되어야 합니다")
    val bank: String,  // 환불 받을 은행

    @field: NotBlank(message = "예금주 이름은 반드시 입력되어야 합니다")
    val depositorName: String // 입금자명
)
