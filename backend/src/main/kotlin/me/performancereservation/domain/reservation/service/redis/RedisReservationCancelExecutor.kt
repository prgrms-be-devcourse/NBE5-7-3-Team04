package me.performancereservation.domain.reservation.service.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository
import me.performancereservation.domain.refund.RefundService
import me.performancereservation.domain.reservation.Reservation
import me.performancereservation.domain.ticket.TicketRepository
import me.performancereservation.global.storage.redis.RedisReservationService
import me.performancereservation.global.storage.redis.RedisSeatService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}


/**
 * 예약 취소 로직 실행기
 */
@Component
class RedisReservationCancelExecutor(
    private val refundService: RefundService,
    private val redisSeatService: RedisSeatService,
    private val redisReservationService: RedisReservationService,
    private val performanceScheduleRepository: PerformanceScheduleRepository,
    private val ticketRepository: TicketRepository
) {

    // 유저가 취소했을 경우 (레디스 좌석 롤백 필요)
    fun executeForUserCancel(reservation: Reservation) {
        execute(reservation, rollbackSeat = true)
    }

    // 공연이 취소됐을 경우 (레디스 좌석 롤백 불필요)
    fun executeForPerformanceCancel(reservation: Reservation) {
        execute(reservation, rollbackSeat = false)
    }

    // 예약 취소 로직 실행 (단일 예약 취소 + 환불 객체 생성)
    private fun execute(reservation: Reservation, rollbackSeat: Boolean) {
        log.info {
            "예약 취소 시작: 예약 ID = ${reservation.id}, 유저 ID = ${reservation.userId}, 공연 회차 ID = ${reservation.scheduleId}, 수량 = ${reservation.quantity}, 결제 상태 = ${reservation.status}"
        }

        // 결제 완료된 예약인 경우 환불 요청 상태로 변경
        if (reservation.isRefundRequired) {
            reservation.requestCancel() // CANCEL_PENDING

            // 티켓 수 만큼 공연 회차의 좌석수를 롤백해서 레디스와의 데이터 정합성 맞춰주기
            performanceScheduleRepository.findByIdOrNull(reservation.scheduleId)
                ?.increaseRemainingSeats(reservation.quantity)

            // 환불 객체 생성
            refundService.save(reservation)
        } else {
            // 결제 미완료 상태라면 바로 취소 확정 처리
            reservation.cancelConfirm() // CANCEL_CONFIRMED
        }

        // 레디스 좌석 롤백이 필요한가?
        if (rollbackSeat) {
            // Redis 좌석 롤백 처리
            redisSeatService.safeIncrement(reservation.scheduleId, reservation.quantity)
        }

        // 예약 만료 처리 대기 큐에서도 제거
        redisReservationService.removeFromPendingExpirationQueue(reservation.id!!)

        // 티켓 취소
        val tickets = ticketRepository.findAllByReservationId(
            reservation.id!!
        )

        tickets.forEach { it.cancel() }
    }
}