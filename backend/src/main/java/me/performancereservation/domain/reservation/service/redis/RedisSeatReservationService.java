package me.performancereservation.domain.reservation.service.redis;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.domain.reservation.mapper.ReservationMapper;
import me.performancereservation.domain.reservation.service.SeatReservationService;
import me.performancereservation.domain.ticket.Ticket;
import me.performancereservation.domain.ticket.TicketRepository;
import me.performancereservation.domain.ticket.enums.TicketStatus;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.storage.redis.RedisReservationService;
import me.performancereservation.global.storage.redis.RedisSeatService;
import org.springframework.stereotype.Service;

/**
 * 유저 단위의 예약, 예약 취소 서비스
 */
@Slf4j
@Service("redisSeatReservationService")
@RequiredArgsConstructor
public class RedisSeatReservationService implements SeatReservationService {
    private final ReservationRepository reservationRepository;
    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final TicketRepository ticketRepository;

    private final RedisSeatService redisSeatService;
    private final RedisReservationService redisReservationService;

    private final ReservationMapper reservationMapper;

    private final RedisReservationCancelExecutor redisReservationCancelExecutor;


    /**
     * 공연 회차에 대해 좌석 선점 후 예약 정보를 생성하고 나서
     * 결제 만료 처리를 위한 시간 정보 등록까지 수행함
     *
     * 1. Redis에서 좌석 선점 (동시성 제어)
     * 2. 좌석이 부족하면 롤백 + 예외 처리
     * 3. Reservation 저장 (상태: PAYMENTS_PENDING - 결제 대기)
     * 4. Redis ZSet에 예약(결제) 만료 정보 등록
     *
     * TODO 추후 어드민 무통장 결제 승인 메서드에서 티켓 수량만큼을 해당 Schedule에 차감 반영해야함
     *
     * @param performanceId 공연 ID
     * @param scheduleId 공연 회차 ID
     * @param userId 예약하는 유저 ID
     * @param quantity 예약한 티켓 수
     * @return ReservationResponse
     */
    @Override
    @Transactional
    public ReservationResponse reserve(Long performanceId, Long scheduleId, Long userId, int quantity) {
        // Redis에서 좌석 1개 먼저 차감 (RDB 접근 없이 선 예약 선점 - 빠른 필터링이 가능한게 장점)
        redisSeatService.safeDecrement(scheduleId, quantity); // 내부에 Redis 좌석 선점 관련 예외처리 있음

        // 예약 엔티티 저장 (status -> PAYMENTS_PENDING (결제 대기))
        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .performanceId(performanceId)
                        .scheduleId(scheduleId)
                        .userId(userId)
                        .quantity(quantity)
                        .status(ReservationStatus.PAYMENTS_PENDING)
                        .build()
        );

        // 결제 대기 중인 예약에 대한 결제 만료 시간을 Redis ZSet에 등록
        // ReservationExpirationScheduler가 주기적으로 조회해서 만료 시간 초과 시 예약을 자동 취소
        redisReservationService.addToPendingExpirationQueue(reservation.getId());

        // 티켓 수량만큼 생성
        for (int i = 0; i < quantity; i++) {
            ticketRepository.save(Ticket.builder()
                    .reservationId(reservation.getId())
                    .performanceId(performanceId)
                    .ticketStatus(TicketStatus.PENDING)
                    .build()
            );
        }

        // response dto mapping용도로 schedulePerformanceInfo 데이터 모델 조회
        SchedulePerformanceInfo schedulePerformanceInfo = performanceScheduleRepository.findSchedulePerformanceInfoByScheduleId(scheduleId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_SCHEDULE_NOT_FOUND.domainException("해당 id의 공연 회차 없음 : " + scheduleId));

        return reservationMapper.toResponseDto(reservation, schedulePerformanceInfo);
    }


    /**
     * 유저가 직접 본인의 예약을 취소함
     * 만약 결제완료가 된 상태면 환불객체를 생성하고 취소 대기상태가 돼야함
     * 결제완료가 안된 상태면 그대로 취소완료처리
     *
     * @param reservationId 예약 ID
     * @param userId 예약한 유저 ID
     */
    @Override
    @Transactional
    public void cancel(Long reservationId, Long userId) {
        // 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ErrorCode.RESERVATION_NOT_FOUND.domainException("예약이 존재하지 않습니다."));

        // 예약이 이미 취소된 상태(CANCEL_CONFIRMED, CANCEL_PENDING)이면 예외 발생
        if (reservation.isAlreadyCanceled()) {
            throw ErrorCode.ALREADY_CANCELED_RESERVATION.domainException("이미 취소된 예약입니다.");
        }

        // 본인 예약만 취소 가능
        if (!reservation.getUserId().equals(userId)) {
            throw ErrorCode.PERMISSION_DENIED.domainException("본인의 예약만 취소할 수 있습니다.");
        }

        // 예약 취소 처리
        redisReservationCancelExecutor.executeForUserCancel(reservation);
    }
}
