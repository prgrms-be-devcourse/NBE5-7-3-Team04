package me.performancereservation.domain.user.repository

import me.performancereservation.domain.user.entitiy.ManagerRequest
import me.performancereservation.domain.user.enums.ManagerRequestStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ManagerRequestRepository : JpaRepository<ManagerRequest, Long> {

    fun existsByUserIdAndStatus(userId: Long, status: ManagerRequestStatus): Boolean

}
