package me.performancereservation.domain.admin.repository;

import me.performancereservation.domain.admin.dto.AdminReservationPageResponse;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminReservationRepository extends JpaRepository<Reservation, Long> {

    /** 관리자가 예약 목록 조회를 위한 조회 쿼리
     *
     * 4 개의 테이블을 조인해 필요한 필드를 dto 로 반환
     * 최신순 출력을 위해 생성 시간 내림차순 정렬
     * @param pageable
     * @return AdminReservationPageResponse
     */
    @Query("""
    SELECT new me.performancereservation.domain.admin.dto.AdminReservationPageResponse(
        r.id,
        p.id,
        s.id,
        u.name,
        p.title,
        p.price,
        r.quantity,
        r.status,
        r.createdAt
    )
    FROM Reservation r
    JOIN User u ON r.userId = u.id
    JOIN PerformanceSchedule s ON r.scheduleId = s.id
    JOIN Performance p ON s.performanceId = p.id
    ORDER BY r.createdAt DESC
""")
    Page<AdminReservationPageResponse> findAdminReservations(Pageable pageable);


    /** 예약 목록 검색 쿼리
     *
     * 검색 조건 (고객 이름, 공연 제목, 예약 상태, 날짜)
     * 이름 + 예약 상태(전체, 대기, 확정 등) + 날짜(구간 필터링, 선택 안하는 경우 전체 검색)
     * 공연 + 예약 상태(전체, 대기, 확정 등) + 날짜(구간 필터링, 선택 안하는 경우 전체 검색)
     * @param userName
     * @param performanceTitle
     * @param reservationStatus
     * @param pageable
     * @return AdminReservationPageResponse
     */
    @Query("""
    SELECT new me.performancereservation.domain.admin.dto.AdminReservationPageResponse(
        r.id,
        p.id,
        s.id,
        u.name,
        p.title,
        p.price,
        r.quantity,
        r.status,
        r.createdAt
    )
    FROM Reservation r
    JOIN User u ON r.userId = u.id
    JOIN PerformanceSchedule s ON r.scheduleId = s.id
    JOIN Performance p ON s.performanceId = p.id
    WHERE 
        (:userName IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :userName, '%'))) 
        AND (:performanceTitle IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :performanceTitle, '%'))) 
        AND (:reservationStatus IS NULL OR r.status = :reservationStatus)
        AND (:startDate IS NULL OR r.createdAt >= :startDate)
        AND (:endDate IS NULL OR r.createdAt <= :endDate)
    ORDER BY r.createdAt DESC
""")
    Page<AdminReservationPageResponse> searchAdminReservations(
            @Param("userName") String userName,
            @Param("performanceTitle") String performanceTitle,
            @Param("reservationStatus") ReservationStatus reservationStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

}
