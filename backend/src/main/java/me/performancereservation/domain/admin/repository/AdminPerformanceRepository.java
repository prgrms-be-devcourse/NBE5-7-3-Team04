package me.performancereservation.domain.admin.repository;

import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminPerformanceRepository extends JpaRepository<Performance, Long> {
    /**
     * 모든 공연을 페이징하여 조회 (생성일자 기준 정렬)
     */
    @Query("SELECT p FROM Performance p ORDER BY p.createdAt DESC")
    Page<Performance> findAllPerformance(Pageable pageable);

    Page<Performance> findAllByStatusOrderByCreatedAt(PerformanceStatus status, Pageable pageable);
}