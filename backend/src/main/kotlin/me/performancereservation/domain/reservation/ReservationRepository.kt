package me.performancereservation.domain.reservation

import me.performancereservation.domain.reservation.enums.ReservationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ReservationRepository : JpaRepository<Reservation, Long> {
    // 예약 id들과 예약 상태를 조건으로 해당하는 예약들을 조회
    @Query(
        """
        SELECT r FROM Reservation r
        WHERE r.id IN :ids AND r.status = :status
    """
    )
    fun findAllByIdsAndStatus(
        @Param("ids") ids: List<Long>,
        @Param("status") status: ReservationStatus
    ): List<Reservation>

    fun findByUserId(userId: Long): Reservation?

    // 공연 회차 id 들로 예약 리스트 조회
    @Query(
        """
        SELECT r FROM Reservation r
        WHERE r.scheduleId IN :scheduleIds
    """
    )
    fun findAllByScheduleIds(@Param("scheduleIds") scheduleIds: List<Long>): List<Reservation>

    fun findAllByScheduleId(scheduleId: Long): List<Reservation>

    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Reservation>

    fun existsByUserIdAndPerformanceId(userId: Long, performanceId: Long): Boolean
}
