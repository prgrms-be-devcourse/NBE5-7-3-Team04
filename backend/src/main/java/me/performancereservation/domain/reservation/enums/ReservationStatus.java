package me.performancereservation.domain.reservation.enums;

public enum ReservationStatus {
    PAYMENTS_PENDING, // 예약 확정 대기 (결제 전)
    PAYMENTS_CONFIRMED, // 예약 확정 (결제 완료)
    CANCEL_PENDING, // 취소 대기
    CANCEL_CONFIRMED // 취소 확정
}
