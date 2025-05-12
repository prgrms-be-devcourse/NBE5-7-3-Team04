package me.performancereservation.global.storage.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisReservationService {

    @Value("${reservation.expired_time}")
    private int RESERVATION_PENDING_EXPIRE_MINUTES; // 예약 만료 시간 TODO 개발 끝나면 제대로 설정

    private final String RESERVATION_EXPIRATION_KEY = "reservation:pending:expiration"; // 만료 시간 값 저장을 위한 ZSet 키

    private final StringRedisTemplate redisTemplate;

    /**
     * 예약 만료를 위해 Redis ZSET에 등록
     *
     * ZSET(정렬된 집합)에 reservationId를 key로
     * now + n분을 만료 시간으로 등록해서
     * 스케줄러가 주기적으로 확인하며 취소처리
     *
     * @param reservationId 예약 ID
     */
    public void addToPendingExpirationQueue(Long reservationId) {
        long expireAt = Instant.now().plusSeconds(RESERVATION_PENDING_EXPIRE_MINUTES * 60L).getEpochSecond();

        redisTemplate.opsForZSet().add(RESERVATION_EXPIRATION_KEY, reservationId.toString(), expireAt);
    }


    /**
     * 예약 만료 대기열(ZSET)에서 해당 예약 ID를 제거
     *
     * 취소가 완료됐거나
     * 결제가 완료되면 (더 이상 만료 처리 대상이 아니면) 호출
     *
     * @param reservationId 예약 ID
     */
    public void removeFromPendingExpirationQueue(Long reservationId) {
        redisTemplate.opsForZSet().remove(RESERVATION_EXPIRATION_KEY, reservationId.toString());
    }


    /**
     * 결제 만료된 예약 ID들을 조회
     *
     * @return 만료된 예약 ID Set
     */
    public Set<String> findExpiredPendingReservationIds() {
        long now = Instant.now().getEpochSecond();

        return redisTemplate.opsForZSet().rangeByScore(RESERVATION_EXPIRATION_KEY, 0, now);
    }
}

