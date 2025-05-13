package me.performancereservation.domain.refund.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateBankInfoRequest(
        @NotNull @Positive Long refundId, // 정보 추가할 환불id
        @NotNull String account, // 환불 받을 계좌번호
        @NotNull String bank, // 환불 받을 은행
        @NotNull String depositorName // 입금자명
) {
}
