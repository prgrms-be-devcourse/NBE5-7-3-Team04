package me.performancereservation.domain.refund.enums

enum class RefundStatus {
    PENDING,  // 계좌입력대기
    READY,  // 계좌입력완료 (환불처리 대기 중)
    CONFIRMED // 환불완료
}
