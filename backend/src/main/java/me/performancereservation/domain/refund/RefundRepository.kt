package me.performancereservation.domain.refund

import me.performancereservation.domain.refund.enums.RefundStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefundRepository : JpaRepository<Refund, Long> {
    fun findRefundByStatus(status: RefundStatus?): List<Refund?>?

    fun findRefundByReservationId(reservationId: Long?): Optional<Refund?>?

    // 주어진 id로 Refund를 찾아 status를 업데이트 하는 메서드
    @Modifying
    @Query("UPDATE Refund r SET r.status = :status WHERE r.id = :id")
    fun updateRefundStatus(
        @Param("id") id: Long,
        @Param("status") status: RefundStatus
    ): Int

    /** 각 Refund마다 id로 join 해서 [Refund, 예약수량, 시작시간, 회차상태, Performance]를 가져와서 리스트로 반환하는 메서드.
     * 예약수량과 시작시간만 가져와야 하는 부분은 column을 지정해서 불필요한 column 조회 줄임 */
    @Query(
        """
            SELECT rf, res.quantity, sch.startTime, p FROM Refund rf
            JOIN Reservation res ON rf.reservationId = res.id
            JOIN PerformanceSchedule sch ON res.scheduleId = sch.id
            JOIN Performance p ON sch.performanceId = p.id
            
            """
    )
    fun findAllRefundsWithDetails(pageable: Pageable): Page<Array<Any?>>

    /** user id로 refund detail response PAGE 반환
     * 위의 findAllRefundsWithDetails에서 where절만 추가 */
    @Query(
        """
            SELECT rf, res.quantity, sch.startTime, p
            FROM Refund rf
            JOIN Reservation res ON rf.reservationId = res.id
            JOIN PerformanceSchedule sch ON res.scheduleId = sch.id
            JOIN Performance p ON sch.performanceId = p.id
            WHERE rf.userId = :userId
            
            """
    )
    fun findRefundsDetailByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable): Page<Array<Any?>>

    /** refund id로 refund detail response PAGE 반환 */
    @Query(
        """ 
            SELECT rf, res.quantity, sch.startTime, p
            FROM Refund rf
            JOIN Reservation res ON rf.reservationId = res.id
            JOIN PerformanceSchedule sch ON res.scheduleId = sch.id
            JOIN Performance p ON sch.performanceId = p.id
            WHERE rf.id = :refundId
            
            """
    )
    fun findRefundsDetailByRefundId(
        @Param("refundId") refundId: Long
    ): List<Array<Any?>>

    /** refund status로 refund detail response PAGE 반환 */
    @Query(
        """
            SELECT rf, res.quantity, sch.startTime, p FROM Refund rf
            JOIN Reservation res ON rf.reservationId = res.id
            JOIN PerformanceSchedule sch ON res.scheduleId = sch.id
            JOIN Performance p ON sch.performanceId = p.id
            WHERE rf.status = :status
            
            """
    )
    fun findRefundsDetailByStatus(
        @Param("status") status: RefundStatus,
        pageable: Pageable?
    ): Page<Array<Any?>>

    /**
     * 주어진 예약 ID 목록에 해당하는 환불의 예약 ID 목록을 조회합니다.
     * @param reservationIds 조회할 예약 ID 목록
     * @return 이미 환불이 생성된 예약 ID 목록
     */
    @Query("SELECT r.reservationId FROM Refund r WHERE r.reservationId IN :reservationIds")
    fun findRefundByReservationIdIn(
        @Param("reservationIds") reservationIds: List<Long?>
    ): List<Long?>

    // 유저 환불 정보
    fun findByUserIdAndReservationId(userId: Long, reservationId: Long): Optional<Refund?>
}
