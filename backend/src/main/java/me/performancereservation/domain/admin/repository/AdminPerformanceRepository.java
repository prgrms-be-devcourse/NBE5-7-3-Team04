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
    /** 관리자가 상태를 이용해 공연 목록을 조회하는 쿼리
     *
     * Performance 테이블에서 받은 상태에 해당하는 공연 목록을 조회
     * 최신순 출력을 위해 생성 시간 내림차순 정렬
     * @param pageable
     * @return Page<Performance>
     */
//    @Query("""
//        SELECT p
//        FROM Performance p
//        WHERE p.status = :status
//        ORDER BY p.createdAt DESC
//    """)
//    Page<Performance> findPendingPerformances(@Param("status") PerformanceStatus status, Pageable pageable);

    Page<Performance> findAllByStatusOrderByCreatedAt(PerformanceStatus status, Pageable pageable);
}