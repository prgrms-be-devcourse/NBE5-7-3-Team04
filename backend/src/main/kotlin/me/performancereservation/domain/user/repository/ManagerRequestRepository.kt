package me.performancereservation.domain.user.repository

import me.performancereservation.domain.user.enums.ManagerRequestStatus
import me.performancereservation.domain.user.entity.ManagerRequest
import org.springframework.data.jpa.repository.JpaRepository

interface ManagerRequestRepository : JpaRepository<ManagerRequest, Long> {
    // 해당 상태의 요청 존재 여부 확인
    fun existsByUserIdAndStatus(userId: Long, status: ManagerRequestStatus): Boolean

    // PENDING 상태의 요청 존재 여부 확인
    fun hasPendingRequest(userId: Long): Boolean {
        return existsByUserIdAndStatus(userId, ManagerRequestStatus.PENDING)
    }

    // APPROVED 상태의 요청 존재 여부 확인
    fun hasApprovedRequest(userId: Long): Boolean {
        return existsByUserIdAndStatus(userId, ManagerRequestStatus.APPROVED)
    }
}