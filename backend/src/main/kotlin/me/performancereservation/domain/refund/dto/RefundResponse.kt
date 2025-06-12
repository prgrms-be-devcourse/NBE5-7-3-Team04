package me.performancereservation.domain.refund.dto

import me.performancereservation.domain.refund.Refund
import me.performancereservation.domain.refund.enums.RefundStatus

data class RefundResponse(
    val refundId: Long,  // 환불 id
    val reservationId: Long,  // 예약 id
    val userId: Long,  // 사용자 id
    val account: String,  // 계좌번호
    val bank: String,  // 환불 상태
    val depositorName: String,
    val status: RefundStatus
) {
    companion object {
        /**
         * Refund 엔티티로부터 RefundResponse를 생성하는 정적 팩토리 메서드
         * @param refund Refund 엔티티
         * @return RefundResponse 객체
         */
        @JvmStatic
        fun fromEntity(refund: Refund): RefundResponse {
            return RefundResponse(
                refund.id!!,
                refund.reservationId,
                refund.userId,
                refund.account!!,
                refund.bank!!,
                refund.depositorName!!,
                refund.status
            )
        }
    }
}
