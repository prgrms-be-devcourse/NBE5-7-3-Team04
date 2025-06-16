package me.performancereservation.domain.reservation.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import me.performancereservation.domain.reservation.Reservation
import me.performancereservation.domain.reservation.ReservationRepository
import me.performancereservation.domain.reservation.enums.ReservationStatus
import me.performancereservation.global.storage.redis.RedisReservationService
import me.performancereservation.global.storage.redis.RedisSeatService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class ReservationExpirationScheduler(
    private val reservationRepository: ReservationRepository,
    private val redisReservationService: RedisReservationService,
    private val redisSeatService: RedisSeatService
) {
    /**
     * 예약 선점을 하고 결제를 일정 시간까지 안하면 자동 예약 취소 처리
     */
    @Transactional
    @Scheduled(fixedDelay = 10000) // 백그라운드에서 10초마다 실행
    fun cancelExpiredReservations() {
        // Redis에서 결제 시간이 만료된 예약들의 id들을 조회
        val expiredReservationIds = redisReservationService.findExpiredPendingReservationIds()
            .map { it.toLong() }

        // 없으면 return
        if (expiredReservationIds.isEmpty()) return

        // in절 쿼리로 해당하는 결제 대기중인 예약들을 조회
        val expiredReservations =
            reservationRepository.findAllByIdsAndStatus(expiredReservationIds, ReservationStatus.PAYMENTS_PENDING)

        expiredReservations.forEach { reservation ->
            try {
                // 예약 취소 관련 처리
                handleReservationCancellation(reservation)
            } catch (e: Exception) {
                log.error(e) { "[예약 만료 스케쥴러] 예약 ID: ${reservation.id} 취소 처리 중 에러 발생" }
            }
        }
    }

    private fun handleReservationCancellation(expiredReservation: Reservation) {
        // 상태 변경 (예약 취소 처리) - 내부 validation 있음
        if (expiredReservation.isCancelable) {
            expiredReservation.cancelConfirm() // 예약 취소확정으로 상태 변경

            log.info { "[예약 만료 스케쥴러] 예약 ID: ${expiredReservation.id} → 상태를 CANCEL_CONFIRMED로 변경 + Redis 좌석 복구" }

        }

        // Redis 좌석 롤백
        redisSeatService.safeIncrement(expiredReservation.scheduleId, expiredReservation.quantity)

        // Redis 만료 예약 제거
        redisReservationService.removeFromPendingExpirationQueue(expiredReservation.id!!)
    }
}