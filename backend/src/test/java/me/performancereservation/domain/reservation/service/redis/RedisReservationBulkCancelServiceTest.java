package me.performancereservation.domain.reservation.service.redis;

import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.global.storage.redis.RedisSeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisReservationBulkCancelServiceTest {

    @Mock
    private PerformanceScheduleRepository performanceScheduleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RedisReservationCancelExecutor redisReservationCancelExecutor;

    @Mock
    private RedisSeatService redisSeatService;

    @InjectMocks
    private RedisReservationBulkCancelService bulkCancelService;

    @Test
    @DisplayName("공연의 모든 예약 일괄 취소 성공")
    void cancelAllByPerformanceId_Success() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L, 2L);
        
        Performance performance = new Performance(
            1L,  // id
            null,  // fileId
            1L,  // managerId
            "콘서트",  // title
            "세종문화회관 대극장",  // venue
            120000,  // price
            2000,  // totalSeats
            PerformanceCategory.CONCERT,  // category
            LocalDateTime.of(2025, 12, 13, 0, 0),  // startDate
            LocalDateTime.of(2025, 12, 14, 0, 0),  // endDate
            "굳",  // description
            PerformanceStatus.CONFIRMED  // status
        );
            
        PerformanceSchedule schedule1 = new PerformanceSchedule(
            1L,  // id
            performanceId,  // performanceId
            LocalDateTime.of(2025, 12, 13, 9, 0),  // startTime
            LocalDateTime.of(2025, 12, 13, 10, 0),  // endTime
            100,  // remainingSeats
            false  // canceled
        );
            
        PerformanceSchedule schedule2 = new PerformanceSchedule(
            2L,  // id
            performanceId,  // performanceId
            LocalDateTime.of(2025, 12, 13, 11, 0),  // startTime
            LocalDateTime.of(2025, 12, 13, 12, 0),  // endTime
            100,  // remainingSeats
            false  // canceled
        );
            
        Reservation reservation1 = new Reservation(
            1L,  // id
            1L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            2,   // quantity
            ReservationStatus.PAYMENTS_PENDING
        );
            
        Reservation reservation2 = new Reservation(
            2L,  // id
            2L,  // userId
            1L,  // performanceId
            2L,  // scheduleId
            1,   // quantity
            ReservationStatus.PAYMENTS_CONFIRMED
        );

        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        // when
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(scheduleIds);
        when(reservationRepository.findAllByScheduleIds(scheduleIds))
            .thenReturn(reservations);

        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(redisSeatService, times(2)).deleteSeatStock(anyLong());
        verify(redisReservationCancelExecutor, times(2)).executeForPerformanceCancel(any(Reservation.class));
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(reservation1);
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(reservation2);
    }

    @Test
    @DisplayName("취소할 예약이 없는 경우")
    void cancelAllByPerformanceId_NoReservations() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L, 2L);
        
        // 일정은 있지만 예약이 없는 경우
        PerformanceSchedule schedule1 = new PerformanceSchedule(
            1L,  // id
            performanceId,  // performanceId
            LocalDateTime.of(2025, 12, 13, 9, 0),  // startTime
            LocalDateTime.of(2025, 12, 13, 10, 0),  // endTime
            100,  // remainingSeats
            false  // canceled
        );
            
        PerformanceSchedule schedule2 = new PerformanceSchedule(
            2L,  // id
            performanceId,  // performanceId
            LocalDateTime.of(2025, 12, 13, 11, 0),  // startTime
            LocalDateTime.of(2025, 12, 13, 12, 0),  // endTime
            100,  // remainingSeats
            false  // canceled
        );

        // when
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(scheduleIds);
        when(reservationRepository.findAllByScheduleIds(scheduleIds))
            .thenReturn(Collections.emptyList());

        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(performanceScheduleRepository).findIdsByPerformanceId(performanceId);
        verify(reservationRepository).findAllByScheduleIds(scheduleIds);
        verify(redisSeatService, never()).deleteSeatStock(anyLong());
        verify(redisReservationCancelExecutor, never()).executeForPerformanceCancel(any(Reservation.class));
    }

    @Test
    @DisplayName("이미 취소된 예약만 있는 경우")
    void cancelAllByPerformanceId_AlreadyCanceled() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L);
        
        // 일정 정보
        PerformanceSchedule schedule = new PerformanceSchedule(
            1L,  // id
            performanceId,  // performanceId
            LocalDateTime.of(2025, 12, 13, 9, 0),  // startTime
            LocalDateTime.of(2025, 12, 13, 10, 0),  // endTime
            100,  // remainingSeats
            false  // canceled
        );
        
        // 이미 취소된 예약들
        Reservation canceledReservation1 = new Reservation(
            1L,  // id
            1L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            2,   // quantity
            ReservationStatus.CANCEL_CONFIRMED
        );

        Reservation canceledReservation2 = new Reservation(
            2L,  // id
            2L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            1,   // quantity
            ReservationStatus.CANCEL_PENDING
        );

        List<Reservation> reservations = Arrays.asList(canceledReservation1, canceledReservation2);

        // when
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(scheduleIds);
        when(reservationRepository.findAllByScheduleIds(scheduleIds))
            .thenReturn(reservations);

        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(performanceScheduleRepository).findIdsByPerformanceId(performanceId);
        verify(reservationRepository).findAllByScheduleIds(scheduleIds);
        verify(redisSeatService, never()).deleteSeatStock(anyLong());
        verify(redisReservationCancelExecutor, never()).executeForPerformanceCancel(any(Reservation.class));
    }
    
    @Test
    @DisplayName("다양한 상태의 예약이 있는 경우")
    void cancelAllByPerformanceId_MixedStatus() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L, 2L);
        
        PerformanceSchedule schedule1 = new PerformanceSchedule(
            1L,  // id
            performanceId,  // performanceId
            LocalDateTime.of(2025, 12, 13, 9, 0),  // startTime
            LocalDateTime.of(2025, 12, 13, 10, 0),  // endTime
            100,  // remainingSeats
            false  // canceled
        );
            
        PerformanceSchedule schedule2 = new PerformanceSchedule(
            2L,  // id
            performanceId,  // performanceId
            LocalDateTime.of(2025, 12, 13, 11, 0),  // startTime
            LocalDateTime.of(2025, 12, 13, 12, 0),  // endTime
            100,  // remainingSeats
            false  // canceled
        );
            
        // 다양한 상태의 예약들
        Reservation pendingReservation = new Reservation(
            1L,  // id
            1L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            2,   // quantity
            ReservationStatus.PAYMENTS_PENDING
        );
            
        Reservation confirmedReservation = new Reservation(
            2L,  // id
            2L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            1,   // quantity
            ReservationStatus.PAYMENTS_CONFIRMED
        );
            
        Reservation canceledReservation = new Reservation(
            3L,
            2L,
            1L,
            3,
            1,
            ReservationStatus.CANCEL_CONFIRMED
        );

        List<Reservation> reservations = Arrays.asList(pendingReservation, confirmedReservation, canceledReservation);

        // when
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(scheduleIds);
        when(reservationRepository.findAllByScheduleIds(scheduleIds))
            .thenReturn(reservations);

        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(performanceScheduleRepository).findIdsByPerformanceId(performanceId);
        verify(reservationRepository).findAllByScheduleIds(scheduleIds);
        
        // 각 예약마다 deleteSeatStock이 호출되어야 함 (취소된 예약 제외)
        verify(redisSeatService, times(2)).deleteSeatStock(1L); // schedule1에 대한 두 번의 호출
        verify(redisSeatService, never()).deleteSeatStock(2L); // schedule2는 취소된 예약이므로 호출되지 않음
        
        // 각 예약마다 executeForPerformanceCancel이 호출되어야 함 (취소된 예약 제외)
        verify(redisReservationCancelExecutor, times(2)).executeForPerformanceCancel(any(Reservation.class));
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(pendingReservation);
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(confirmedReservation);
    }

    @Test
    @DisplayName("특정 일정의 모든 예약 일괄 취소 성공")
    void cancelAllByScheduleId_Success() {
        // given
        Long scheduleId = 1L;
        
        // 취소할 예약들
        Reservation pendingReservation = new Reservation(
            1L,  // id
            1L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            2,   // quantity
            ReservationStatus.PAYMENTS_PENDING
        );
            
        Reservation confirmedReservation = new Reservation(
            2L,  // id
            2L,  // userId
            1L,  // performanceId
            2L,  // scheduleId
            1,   // quantity
            ReservationStatus.PAYMENTS_CONFIRMED
        );

        List<Reservation> reservations = Arrays.asList(pendingReservation, confirmedReservation);

        // when
        when(reservationRepository.findAllByScheduleId(scheduleId))
            .thenReturn(reservations);

        bulkCancelService.cancelAllByScheduleId(scheduleId);

        // then
        verify(reservationRepository).findAllByScheduleId(scheduleId);
        verify(redisSeatService).deleteSeatStock(scheduleId);
        verify(redisReservationCancelExecutor, times(2)).executeForPerformanceCancel(any(Reservation.class));
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(pendingReservation);
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(confirmedReservation);
    }

    @Test
    @DisplayName("특정 일정에 취소할 예약이 없는 경우")
    void cancelAllByScheduleId_NoReservations() {
        // given
        Long scheduleId = 1L;

        // when
        when(reservationRepository.findAllByScheduleId(scheduleId))
            .thenReturn(Collections.emptyList());

        bulkCancelService.cancelAllByScheduleId(scheduleId);

        // then
        verify(reservationRepository).findAllByScheduleId(scheduleId);
        verify(redisSeatService, never()).deleteSeatStock(anyLong());
        verify(redisReservationCancelExecutor, never()).executeForPerformanceCancel(any(Reservation.class));
    }

    @Test
    @DisplayName("특정 일정에 이미 취소된 예약만 있는 경우")
    void cancelAllByScheduleId_AlreadyCanceled() {
        // given
        Long scheduleId = 1L;
        
        // 이미 취소된 예약들
        Reservation canceledReservation1 = new Reservation(
            1L,  // id
            1L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            2,   // quantity
            ReservationStatus.CANCEL_CONFIRMED
        );

        Reservation canceledReservation2 = new Reservation(
            2L,  // id
            2L,  // userId
            1L,  // performanceId
            1L,  // scheduleId
            1,   // quantity
            ReservationStatus.CANCEL_PENDING
        );

        List<Reservation> reservations = Arrays.asList(canceledReservation1, canceledReservation2);

        // when
        when(reservationRepository.findAllByScheduleId(scheduleId))
            .thenReturn(reservations);

        bulkCancelService.cancelAllByScheduleId(scheduleId);

        // then
        verify(reservationRepository).findAllByScheduleId(scheduleId);
        verify(redisSeatService).deleteSeatStock(scheduleId);
        verify(redisReservationCancelExecutor, never()).executeForPerformanceCancel(any(Reservation.class));
    }
} 