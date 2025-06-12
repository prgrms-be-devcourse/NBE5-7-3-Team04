package me.performancereservation.domain.user.enums

enum class ManagerRequestStatus {
    PENDING,  // 권한 요청해둔 상태
    REJECTED,  // 권한 요청 거절
    APPROVED // 권한 요청 승인
}