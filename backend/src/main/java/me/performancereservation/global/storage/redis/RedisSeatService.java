package me.performancereservation.global.storage.redis;

import lombok.RequiredArgsConstructor;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSeatService {
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.data.redis.schedule-seats}")
    private String stockPrefix;

    /**
     * Redis에 공연 회차별 좌석 재고를 저장할 때 사용하는 키
     * scheduleId에 따라 각 회차의 좌석 정보를 구분
     */
    private String key(Long scheduleId) {
        return stockPrefix + scheduleId;
    }

    /**
     * 공연 회차별 좌석 수를 Redis에 초기값으로 저장
     * 이 좌석 수는 예약 선점 및 동시성 제어의 기준이 됨
     * TODO 공연회차 생성할 때 이 메서드 같이 호출해서 초기화 해줘야함!
     *
     * @param scheduleId 공연 회차 ID (Long)
     * @param stock 좌석 수 (int)
     */
    public void initializeSeatStock(Long scheduleId, int stock) {
        redisTemplate.opsForValue().set(key(scheduleId), String.valueOf(stock));
    }

    /**
     * Redis에 저장된 공연 회차의 좌석 수를 차감
     * 예약 시 좌석 선점 단계에서 사용되고 동시성 제어를 위해 원자적 연산으로 처리됨
     *
     * @param scheduleId 공연 회차 ID (Long)
     * @param quantity 차감할 좌석 수 (int)
     */
    public void safeDecrement(Long scheduleId, int quantity) {
        Long result = redisTemplate.opsForValue().decrement(key(scheduleId), quantity); // decr 연산 (atomic 함)

        if (result == null) {
            throw ErrorCode.SEAT_STOCK_DECREMENT_FAILED.serviceException("Redis decr 연산 결과가 null임"); // 없는 회차거나 공연 생성 시 초기화 오류
        }

        // 좌석이 부족한 경우 Redis 좌석 감소 처리 했던 좌석 롤백
        if (result < 0) {
            Long restoredSeats = redisTemplate.opsForValue().increment(key(scheduleId), quantity);

            throw ErrorCode.NO_REMAINING_SEATS.domainException("남은 좌석 없음.. 남은좌석: " + restoredSeats + "티켓 수: " + quantity);
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
    public int safeIncrement(Long scheduleId, int quantity) {
        Long result = redisTemplate.opsForValue().increment(key(scheduleId), quantity); // incr 연산 (atomic 함)

        if (result == null) {
            throw ErrorCode.SEAT_STOCK_INCREMENT_FAILED.serviceException("Redis incr 연산 결과가 null임"); // 없는 회차거나 공연 생성 시 초기화 오류
        }

        return result.intValue();
    }
}
