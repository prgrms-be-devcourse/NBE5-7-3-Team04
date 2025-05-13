package me.performancereservation.domain.performance.repository;

import me.performancereservation.domain.performance.entities.Performance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Page<Performance> findByManagerId(Long managerId, Pageable pageable);

    @Query("""
        SELECT DISTINCT p
        FROM Performance p
        JOIN PerformanceSchedule ps ON p.id = ps.performanceId
        WHERE ps.remainingSeats > 0
        AND p.status = 'CONFIRMED'
    """)
    Page<Performance> findAvailablePerformances(Pageable pageable);
}
