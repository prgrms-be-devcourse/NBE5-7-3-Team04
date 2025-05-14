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
        
        Performance performance = Performance.builder()
            .id(performanceId)
            .title("오페라 갈라")
            .venue("세종문화회관 대극장")
            .price(120000)
            .totalSeats(2000)
            .category(PerformanceCategory.OPERA)
            .performanceDate(LocalDateTime.of(2025, 12, 13, 0, 0))
            .description("한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!")
            .status(PerformanceStatus.CONFIRMED)
            .build();
            
        PerformanceSchedule schedule1 = PerformanceSchedule.builder()
            .id(1L)
            .performanceId(performanceId)
            .startTime(LocalDateTime.of(2025, 12, 13, 9, 0))
            .endTime(LocalDateTime.of(2025, 12, 13, 10, 0))
            .remainingSeats(100)
            .canceled(false)
            .build();
            
        PerformanceSchedule schedule2 = PerformanceSchedule.builder()
            .id(2L)
            .performanceId(performanceId)
            .startTime(LocalDateTime.of(2025, 12, 13, 11, 0))
            .endTime(LocalDateTime.of(2025, 12, 13, 12, 0))
            .remainingSeats(100)
            .canceled(false)
            .build();
            
        Reservation reservation1 = Reservation.builder()
            .id(1L)
            .scheduleId(1L)
            .userId(1L)
            .quantity(2)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
            
        Reservation reservation2 = Reservation.builder()
            .id(2L)
            .scheduleId(2L)
            .userId(2L)
            .quantity(1)
            .status(ReservationStatus.PAYMENTS_CONFIRMED)
            .build();

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
        PerformanceSchedule schedule1 = PerformanceSchedule.builder()
            .id(1L)
            .performanceId(performanceId)
            .startTime(LocalDateTime.of(2025, 12, 13, 9, 0))
            .endTime(LocalDateTime.of(2025, 12, 13, 10, 0))
            .remainingSeats(100)
            .canceled(false)
            .build();
            
        PerformanceSchedule schedule2 = PerformanceSchedule.builder()
            .id(2L)
            .performanceId(performanceId)
            .startTime(LocalDateTime.of(2025, 12, 13, 11, 0))
            .endTime(LocalDateTime.of(2025, 12, 13, 12, 0))
            .remainingSeats(100)
            .canceled(false)
            .build();

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
        PerformanceSchedule schedule = PerformanceSchedule.builder()
            .id(1L)
            .performanceId(performanceId)
            .startTime(LocalDateTime.of(2025, 12, 13, 9, 0))
            .endTime(LocalDateTime.of(2025, 12, 13, 10, 0))
            .remainingSeats(100)
            .canceled(false)
            .build();
        
        // 이미 취소된 예약들
        Reservation canceledReservation1 = Reservation.builder()
            .id(1L)
            .scheduleId(1L)
            .userId(1L)
            .quantity(2)
            .status(ReservationStatus.CANCEL_CONFIRMED)
            .build();

        Reservation canceledReservation2 = Reservation.builder()
            .id(2L)
            .scheduleId(1L)
            .userId(2L)
            .quantity(1)
            .status(ReservationStatus.CANCEL_PENDING)
            .build();

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
    @DisplayName("공연에 대한 일정이 없는 경우 취소 처리")
    void cancelAllByPerformanceId_NoSchedules() {
        // given
        Long performanceId = 1L;
        List<Long> emptyScheduleIds = Collections.emptyList();
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(emptyScheduleIds);

        // when
        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(performanceScheduleRepository).findIdsByPerformanceId(performanceId);
        verify(redisSeatService, never()).deleteSeatStock(anyLong());
        verify(reservationRepository, never()).findAllByScheduleIds(anyList());
        verify(redisReservationCancelExecutor, never()).executeForPerformanceCancel(any(Reservation.class));
    }
    
    @Test
    @DisplayName("다양한 상태의 예약이 있는 경우")
    void cancelAllByPerformanceId_MixedStatus() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L, 2L);
        
        PerformanceSchedule schedule1 = PerformanceSchedule.builder()
            .id(1L)
            .performanceId(performanceId)
            .startTime(LocalDateTime.of(2025, 12, 13, 9, 0))
            .endTime(LocalDateTime.of(2025, 12, 13, 10, 0))
            .remainingSeats(100)
            .canceled(false)
            .build();
            
        PerformanceSchedule schedule2 = PerformanceSchedule.builder()
            .id(2L)
            .performanceId(performanceId)
            .startTime(LocalDateTime.of(2025, 12, 13, 11, 0))
            .endTime(LocalDateTime.of(2025, 12, 13, 12, 0))
            .remainingSeats(100)
            .canceled(false)
            .build();
            
        Reservation pendingReservation = Reservation.builder()
            .id(1L)
            .scheduleId(1L)
            .userId(1L)
            .quantity(2)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
            
        Reservation confirmedReservation = Reservation.builder()
            .id(2L)
            .scheduleId(2L)
            .userId(2L)
            .quantity(1)
            .status(ReservationStatus.PAYMENTS_CONFIRMED)
            .build();
            
        Reservation canceledReservation = Reservation.builder()
            .id(3L)
            .scheduleId(1L)
            .userId(3L)
            .quantity(3)
            .status(ReservationStatus.CANCEL_CONFIRMED)
            .build();

        List<Reservation> reservations = Arrays.asList(
            pendingReservation,
            confirmedReservation,
            canceledReservation
        );

        // when
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(scheduleIds);
        when(reservationRepository.findAllByScheduleIds(scheduleIds))
            .thenReturn(reservations);

        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(redisSeatService, times(2)).deleteSeatStock(anyLong());
        verify(redisReservationCancelExecutor, times(2)).executeForPerformanceCancel(any(Reservation.class));
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(pendingReservation);
        verify(redisReservationCancelExecutor).executeForPerformanceCancel(confirmedReservation);
        verify(redisReservationCancelExecutor, never()).executeForPerformanceCancel(canceledReservation);
    }
} 