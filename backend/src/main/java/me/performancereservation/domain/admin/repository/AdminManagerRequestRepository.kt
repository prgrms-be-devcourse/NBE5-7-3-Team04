package me.performancereservation.domain.admin.repository

import me.performancereservation.domain.user.entitiy.ManagerRequest
import me.performancereservation.domain.user.enums.ManagerRequestStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AdminManagerRequestRepository : JpaRepository<ManagerRequest, Long> {
    fun findAllByStatusOrderByCreatedAt(status: ManagerRequestStatus, pageable: Pageable): Page<ManagerRequest>
}
