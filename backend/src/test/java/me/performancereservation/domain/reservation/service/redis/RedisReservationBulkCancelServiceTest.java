package me.performancereservation.domain.reservation.service.redis;

import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisReservationBulkCancelServiceTest {

    @Mock
    private PerformanceScheduleRepository performanceScheduleRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RedisReservationCancelExecutor redisReservationCancelExecutor;

    @InjectMocks
    private RedisReservationBulkCancelService bulkCancelService;

    @Test
    @DisplayName("정상적인 일괄 취소")
    void cancelAllByPerformanceId_Success() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L, 2L);
        
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
        verify(redisReservationCancelExecutor, times(2)).execute(any(Reservation.class));
    }

    @Test
    @DisplayName("취소할 예약이 없는 경우")
    void cancelAllByPerformanceId_NoReservations() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L, 2L);

        // when
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(scheduleIds);
        when(reservationRepository.findAllByScheduleIds(scheduleIds))
            .thenReturn(List.of());

        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(redisReservationCancelExecutor, never()).execute(any(Reservation.class));
    }

    @Test
    @DisplayName("이미 취소된 예약이 있는 경우")
    void cancelAllByPerformanceId_AlreadyCanceled() {
        // given
        Long performanceId = 1L;
        List<Long> scheduleIds = Arrays.asList(1L);
        
        Reservation canceledReservation = Reservation.builder()
            .id(1L)
            .scheduleId(1L)
            .userId(1L)
            .quantity(2)
            .status(ReservationStatus.CANCEL_CONFIRMED)
            .build();

        List<Reservation> reservations = List.of(canceledReservation);

        // when
        when(performanceScheduleRepository.findIdsByPerformanceId(performanceId))
            .thenReturn(scheduleIds);
        when(reservationRepository.findAllByScheduleIds(scheduleIds))
            .thenReturn(reservations);

        bulkCancelService.cancelAllByPerformanceId(performanceId);

        // then
        verify(redisReservationCancelExecutor, never()).execute(any(Reservation.class));
    }
} 