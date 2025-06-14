package me.performancereservation.domain.settlement

import me.performancereservation.domain.settlement.dto.SettlementResponse
import me.performancereservation.domain.settlement.enums.SettlementStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SettlementRepository : JpaRepository<Settlement, Long> {
    // 공연 관리자의 정산 목록 조회 (Performance와 조인하여 title 포함)
    @Query(
        """
            SELECT new me.performancereservation.domain.settlement.dto.SettlementResponse(
            s.id, s.totalAmount, s.settledAt, s.account, s.bank, s.status, p.title)
            FROM Settlement s
            JOIN Performance p ON s.performanceId = p.id
            WHERE p.managerId = :userId
            
        """
    )
    fun findAllSettlementsWithUserId(@Param("userId") userId: Long, pageable: Pageable): Page<SettlementResponse>

    // 전체 정산 목록 조회 (Performance와 조인하여 title 포함)
    @Query(
        """
            SELECT new me.performancereservation.domain.settlement.dto.SettlementResponse(
            s.id, s.totalAmount, s.settledAt, s.account, s.bank, s.status, p.title)
            FROM Settlement s
            JOIN Performance p ON s.performanceId = p.id
            
        """
    )
    fun findAllSettlements(pageable: Pageable): Page<SettlementResponse>

    // 상태별 정산 목록 조회 (Performance와 조인하여 title 포함)
    @Query(
        """
            SELECT new me.performancereservation.domain.settlement.dto.SettlementResponse(
            s.id, s.totalAmount, s.settledAt, s.account, s.bank, s.status, p.title)
            FROM Settlement s
            JOIN Performance p ON s.performanceId = p.id
            WHERE s.status = :status
            
        """
    )
    fun findAllSettlementsByStatus(
        @Param("status") status: SettlementStatus,
        pageable: Pageable
    ): Page<SettlementResponse>

    fun findSettlementByPerformanceId(performanceId: Long): List<Settlement>
}
