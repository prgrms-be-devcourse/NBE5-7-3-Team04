package me.performancereservation.domain.refund.dto;

import me.performancereservation.domain.refund.Refund;
import me.performancereservation.domain.refund.enums.RefundStatus;

public record RefundResponse(
    long refundId, // 환불 id
    long reservationId, // 예약 id
    long userId, // 사용자 id
    String account, // 계좌번호
    String bank, // 환불 상태
    String depositorName,
    RefundStatus status
) {
    /**
     * Refund 엔티티로부터 RefundResponse를 생성하는 정적 팩토리 메서드
     * @param refund Refund 엔티티
     * @return RefundResponse 객체
     */
    public static RefundResponse fromEntity(Refund refund) {
        return new RefundResponse(
            refund.getId(),
            refund.getReservationId(),
            refund.getUserId(),
            refund.getAccount(),
            refund.getBank(),
            refund.getDepositorName(),
            refund.getStatus()
        );
    }
}
