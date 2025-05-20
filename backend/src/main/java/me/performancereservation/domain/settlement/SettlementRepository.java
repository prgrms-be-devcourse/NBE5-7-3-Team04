package me.performancereservation.domain.settlement;

import me.performancereservation.domain.settlement.dto.SettlementResponse;
import me.performancereservation.domain.settlement.enums.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    // 공연 관리자의 정산 목록 조회 (Performance와 조인하여 title 포함)
    @Query("""
            SELECT new me.performancereservation.domain.settlement.dto.SettlementResponse(
            s.id, s.totalAmount, s.settledAt, s.account, s.bank, s.status, p.title)
            FROM Settlement s
            JOIN Performance p ON s.performanceId = p.id
            WHERE p.managerId = :userId
            """)
    Page<SettlementResponse> findAllSettlementsWithUserId(@Param("userId") Long userId, Pageable pageable);
    
    // 전체 정산 목록 조회 (Performance와 조인하여 title 포함)
    @Query("""
            SELECT new me.performancereservation.domain.settlement.dto.SettlementResponse(
            s.id, s.totalAmount, s.settledAt, s.account, s.bank, s.status, p.title)
            FROM Settlement s
            JOIN Performance p ON s.performanceId = p.id
            """)
    Page<SettlementResponse> findAllSettlements(Pageable pageable);
    
    // 상태별 정산 목록 조회 (Performance와 조인하여 title 포함)
    @Query("""
            SELECT new me.performancereservation.domain.settlement.dto.SettlementResponse(
            s.id, s.totalAmount, s.settledAt, s.account, s.bank, s.status, p.title)
            FROM Settlement s
            JOIN Performance p ON s.performanceId = p.id
            WHERE s.status = :status
            """)
    Page<SettlementResponse> findAllSettlementsByStatus(@Param("status") SettlementStatus status, Pageable pageable);

    List<Settlement> findSettlementByPerformanceId(Long performanceId);
}
