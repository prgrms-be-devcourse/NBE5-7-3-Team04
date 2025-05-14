package me.performancereservation.domain.performance.repository;

import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Page<Performance> findByManagerId(Long managerId, Pageable pageable);

    /** 예약 가능한 전체 공연 검색
     *
     * @param pageable
     */
    @Query("""
        SELECT DISTINCT p
        FROM Performance p
        JOIN PerformanceSchedule ps ON p.id = ps.performanceId
        WHERE ps.remainingSeats > 0
        AND p.status = 'CONFIRMED'
        ORDER BY p.startDate DESC
    """)
    Page<Performance> findAvailablePerformances(Pageable pageable);


    /** 예약이 가능한 공연중 검색
     *
     * @param title 제목
     * @param venue 공연장 위치
     * @param start 시작 날짜(필터링)
     * @param end 종료 날짜(필터링)
     * @param pageable 페이징
     * @return performance
     */
    @Query("""
    SELECT DISTINCT p
    FROM Performance p
    JOIN PerformanceSchedule ps ON p.id = ps.performanceId
    WHERE ps.remainingSeats > 0
    AND p.status = 'CONFIRMED'
    AND (:category IS NULL OR p.status = :category)
    AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
    AND (:venue IS NULL OR LOWER(p.venue) LIKE LOWER(CONCAT('%', :venue, '%')))
    AND (:start IS NULL OR p.endDate >= :start)
    AND (:end IS NULL OR p.startDate <= :end)
    ORDER BY p.startDate DESC
""")
    Page<Performance> searchAvailablePerformances(
            @Param("title") String title,
            @Param("venue") String venue,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("category") PerformanceCategory category,
            Pageable pageable
    );


    /** 공연 관리자용 검색
     *
     * @param managerId 관리자 id
     * @param status 상태
     * @param title 제목
     * @param venue 공연장
     * @param start 날짜 필터링
     * @param end 날짜 필터링
     * @param pageable 페이징
     * @return performance
     */
    @Query("""
    SELECT p 
    FROM Performance p 
    WHERE p.managerId = :managerId 
    AND (:status IS NULL OR p.status = :status)
    AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
    AND (:venue IS NULL OR LOWER(p.venue) LIKE LOWER(CONCAT('%', :venue, '%')))
    AND (:start IS NULL OR p.endDate >= :start)
    AND (:end IS NULL OR p.startDate <= :end)
    ORDER BY p.startDate DESC
""")
    Page<Performance> searchManagerPerformances(
            @Param("managerId") Long managerId,
            @Param("status") PerformanceStatus status,
            @Param("title") String title,
            @Param("venue") String venue,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 종료일 지났고 종료처리 되지 않은 공연을 가져오는 메서드
    List<Performance> findByEndDateBeforeAndStatus(LocalDateTime endDate, PerformanceStatus status);
}
