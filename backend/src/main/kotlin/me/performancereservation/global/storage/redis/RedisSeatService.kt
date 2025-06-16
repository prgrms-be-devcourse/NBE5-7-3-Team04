package me.performancereservation.global.storage.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.RequiredArgsConstructor
import me.performancereservation.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}


@Service
class RedisSeatService(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${spring.data.redis.schedule-seats}")
    private val stockPrefix: String
) {

    /**
     * Redis에 공연 회차별 좌석 재고를 저장할 때 사용하는 키
     * scheduleId에 따라 각 회차의 좌석 정보를 구분
     */
    private fun key(scheduleId: Long): String = "$stockPrefix$scheduleId"

    /**
     * 공연 회차별 좌석 수를 Redis에 초기값으로 저장
     * 이 좌석 수는 예약 선점 및 동시성 제어의 기준이 됨
     * TODO 공연회차 생성할 때 이 메서드 같이 호출해서 초기화 해줘야함!
     *
     * @param scheduleId 공연 회차 ID (Long)
     * @param stock 좌석 수 (int)
     */
    fun initializeSeatStock(scheduleId: Long, stock: Int) {
        redisTemplate.opsForValue()[key(scheduleId)] = stock.toString()
    }

    /**
     * 공연 회차별 좌석 수를 Redis에서 제거
     * 공연이 취소되거나 끝나면 제거해줘야 함
     *
     * @param scheduleId 공연 회차 ID
     */
    fun deleteSeatStock(scheduleId: Long) {
        redisTemplate.delete(key(scheduleId))
    }

    /**
     * Redis에 저장된 공연 회차의 좌석 수를 차감
     * 예약 시 좌석 선점 단계에서 사용되고 동시성 제어를 위해 원자적 연산으로 처리됨
     *
     * @param scheduleId 공연 회차 ID (Long)
     * @param quantity 차감할 좌석 수 (int)
     */
    fun safeDecrement(scheduleId: Long, quantity: Int) {
        val redisKey = key(scheduleId)

        log.info {
            "redisKey $redisKey"
        }

        // decr 연산 (atomic 함)
        val result = redisTemplate.opsForValue().decrement(redisKey, quantity.toLong())
            ?: throw ErrorCode.SEAT_STOCK_DECREMENT_FAILED.serviceException("Redis decr 연산 결과가 null임") // 없는 회차거나 공연 생성 시 초기화 오류

        log.info {
            "decr 연산 호출됨 result: $result"
        }

        // 좌석이 부족한 경우 Redis 좌석 감소 처리 했던 좌석 롤백
        if (result < 0) {
            val restoredSeats = redisTemplate.opsForValue().increment(redisKey, quantity.toLong())

            throw ErrorCode.NO_REMAINING_SEATS.domainException(
                "남은 좌석 없음.. 남은좌석: " + restoredSeats + "티켓 수: " + quantity
            )
        }
    }

    /**
     * Redis에 저장된 공연 회차의 좌석 수를 복원
     * 예약 실패(결제 완료 전 예약취소, 타임아웃 등등) 시 선점했던 좌석 수를 되돌리는 데 사용됨
     *
     * @param scheduleId 공연 회차 ID (Long)
     * @param quantity 복원할 좌석 수 (int)
     * @return 복원 후 남은좌석 수 (int)
     */
    fun safeIncrement(scheduleId: Long, quantity: Int): Int {
        return redisTemplate.opsForValue().increment(key(scheduleId), quantity.toLong())
            ?.toInt()
            ?: throw ErrorCode.SEAT_STOCK_INCREMENT_FAILED.serviceException("Redis incr 연산 결과가 null임") // 없는 회차거나 공연 생성 시 초기화 오류
    }
}
