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
    Page<Performance> findAllByStatusOrderByCreatedAt(PerformanceStatus status, Pageable pageable);
}