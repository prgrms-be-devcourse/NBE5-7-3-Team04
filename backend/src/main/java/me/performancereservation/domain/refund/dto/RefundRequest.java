package me.performancereservation.domain.refund.dto;

public record RefundRequest(
    Long reservationId, // (FK) 환불을 요청하는 예약 id

    Long userId, // (FK) 환불을 요청한 유저의 id

    String account, // 환불 받을 계좌번호

    String bank // 환불 받을 은행
) {
}
