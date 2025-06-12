package me.performancereservation.domain.performance.enums

enum class ScheduleStatus {
    AVAILABLE,  // 예매 가능
    SOLD_OUT,  // 매진
    IN_PROGRESS,  // 공연 중
    COMPLETED,  // 공연 완료
    CANCELLED // 취소됨
}