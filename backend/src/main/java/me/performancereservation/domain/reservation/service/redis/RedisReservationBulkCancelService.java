package me.performancereservation.domain.reservation.service.redis;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.sms.SMSService;
import me.performancereservation.global.storage.redis.RedisSeatService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 자동/수동 예약 일괄 취소 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisReservationBulkCancelService {
    private final SMSService smsService;
    private final RedisSeatService redisSeatService;

    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final ReservationRepository reservationRepository;

    private final RedisReservationCancelExecutor redisReservationCancelExecutor;

    /**
     * 공연 id를 기반으로 해당 공연에 대한 예약들을 일괄 취소
     * TODO 현재 실질적인 벌크처리는 안되고 있음
     *      만약 공연취소로 인한 이벤트가 발생해서 이 메서드가 실행되는 경우엔, 백그라운드에서 동작하기때문에 상관없지만
     *      수동으로 호출하는 경우엔, 사용자 경험이 안좋을 수 있음 (mvp 구현 목적으로 추후 벌크 update 방식으로 리팩토링 예정)
     *
     * @param performanceId 취소된 공연 id
     */
    @Transactional
    public void cancelAllByPerformanceId(Long performanceId) {
        log.info("[공연 일괄 취소] 공연 id: {}", performanceId);

        // 취소된 공연의 모든 회차 id 조회
        List<Long> scheduleIds = performanceScheduleRepository.findIdsByPerformanceId(performanceId);
        
        // 공연 일정이 없으면 패스
        if (scheduleIds.isEmpty()) return;

        // 회차별 예약 목록 조회
        List<Reservation> reservations = reservationRepository.findAllByScheduleIds(scheduleIds);
        
        // 예약이 없으면 패스
        if (reservations.isEmpty()) return;

        // 레디스 좌석 정보 제거 및 예약 취소 처리
        for (Reservation reservation : reservations) {
            // 이미 취소된 예약은 패스
            if (reservation.isAlreadyCanceled()) continue;

            // 레디스 좌석 정보 제거
            redisSeatService.deleteSeatStock(reservation.getScheduleId());
            
            // 예약 취소 처리
            redisReservationCancelExecutor.executeForPerformanceCancel(reservation);

            // TODO 시연시 주석 제거 근데 이건 그대로 주석처리 해도 될것 같기도 논의 필요?
            // 공연 취소 안내 문자
//            smsService.performanceCanceled(reservation);
        }
    }

    /**
     * 공연 회차 id를 기반으로 해당 회차에 대한 예약들을 일괄 취소
     * TODO 공연 취소 이벤트 처리와 마찬가지로 벌크 처리 아직 구현 x
     *
     * @param scheduleId 취소된 공연 id
     */
    @Transactional
    public void cancelAllByScheduleId(Long scheduleId) {
        log.info("[회차 일괄 취소] 회차 id: {}", scheduleId);

        // 회차 예약 목록 조회
        List<Reservation> reservations = reservationRepository.findAllByScheduleId(scheduleId);

        // 예약이 없으면 패스
        if (reservations.isEmpty()) return;

        // 레디스 좌석 정보 제거
        redisSeatService.deleteSeatStock(scheduleId);

        for (Reservation reservation : reservations) {
            // 이미 취소된 예약은 패스
            if (reservation.isAlreadyCanceled()) continue;

            // 예약 취소 처리
            redisReservationCancelExecutor.executeForPerformanceCancel(reservation);

            // TODO 시연시 주석 제거 근데 이건 그대로 주석처리 해도 될것 같기도 논의 필요?
            // 공연 취소 안내 문자
//            smsService.performanceCanceled(reservation);
        }
    }
}