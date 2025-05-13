package me.performancereservation.domain.reservation.service.redis;

import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.domain.reservation.mapper.ReservationMapper;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.storage.redis.RedisReservationService;
import me.performancereservation.global.storage.redis.RedisSeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisSeatReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private PerformanceScheduleRepository performanceScheduleRepository;
    
    @Mock
    private RedisSeatService redisSeatService;
    
    @Mock
    private RedisReservationService redisReservationService;
    
    @Mock
    private ReservationMapper reservationMapper;
    
    @InjectMocks
    private RedisSeatReservationService reservationService;
    
    @Test
    @DisplayName("정상적인 예약 시도")
    void reserve_Success() {
        // given
        Long scheduleId = 1L;
        Long userId = 1L;
        int quantity = 2;
        
        SchedulePerformanceInfo scheduleInfo = new SchedulePerformanceInfo(
            1L, // performanceId
            "Test Performance",
            "Test Venue",
            10000,
                scheduleId, // scheduleId
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2)
        );
        
        Reservation reservation = Reservation.builder()
            .id(1L)
            .scheduleId(scheduleId)
            .userId(userId)
            .quantity(quantity)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
        
        ReservationResponse expectedResponse = new ReservationResponse(
                scheduleId,
            "Test Performance",
            "Test Venue",
            quantity,
            ReservationStatus.PAYMENTS_PENDING,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(30),
            10000,
            20000
        );
        
        // when
        when(performanceScheduleRepository.findSchedulePerformanceInfoByScheduleId(scheduleId))
            .thenReturn(Optional.of(scheduleInfo));
        when(reservationRepository.save(any(Reservation.class)))
            .thenReturn(reservation);
        when(reservationMapper.toResponseDto(any(Reservation.class), any(SchedulePerformanceInfo.class)))
            .thenReturn(expectedResponse);
        
        ReservationResponse result = reservationService.reserve(scheduleId, userId, quantity);
        
        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(redisSeatService).safeDecrement(scheduleId, quantity);
        verify(redisReservationService).addToPendingExpirationQueue(anyLong());
    }
    
    @Test
    @DisplayName("좌석이 부족한 경우 예약 시도")
    void reserve_NoRemainingSeats() {
        // given
        Long scheduleId = 1L;
        Long userId = 1L;
        int quantity = 2;
        
        // when
        doThrow(ErrorCode.NO_REMAINING_SEATS.domainException("남은 좌석 없음"))
            .when(redisSeatService).safeDecrement(scheduleId, quantity);
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            reservationService.reserve(scheduleId, userId, quantity)
        );
        
        assertEquals(ErrorCode.NO_REMAINING_SEATS, exception.getErrorCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(redisReservationService, never()).addToPendingExpirationQueue(anyLong());
    }
    
    @Test
    @DisplayName("존재하지 않는 공연 회차에 대한 예약 시도")
    void reserve_ScheduleNotFound() {
        // given
        Long scheduleId = 1L;
        Long userId = 1L;
        int quantity = 2;
        
        // when
        doThrow(ErrorCode.SEAT_STOCK_DECREMENT_FAILED.serviceException("Redis decr 연산 결과가 null임"))
            .when(redisSeatService).safeDecrement(scheduleId, quantity);
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            reservationService.reserve(scheduleId, userId, quantity)
        );
        
        assertEquals(ErrorCode.SEAT_STOCK_DECREMENT_FAILED, exception.getErrorCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(redisReservationService, never()).addToPendingExpirationQueue(anyLong());
    }
    
    @Test
    @DisplayName("정상적인 예약 취소")
    void cancel_Success() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long scheduleId = 1L;
        int quantity = 2;
        
        Reservation reservation = Reservation.builder()
            .id(reservationId)
            .userId(userId)
            .scheduleId(scheduleId)
            .quantity(quantity)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.of(reservation));
        
        reservationService.cancel(reservationId, userId);
        
        // then
        assertEquals(ReservationStatus.CANCEL_CONFIRMED, reservation.getStatus());
        verify(redisSeatService).safeIncrement(scheduleId, quantity);
        verify(redisReservationService).removeFromPendingExpirationQueue(reservationId);
    }
    
    @Test
    @DisplayName("이미 취소된 예약 취소 시도")
    void cancel_AlreadyCanceled() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        
        Reservation reservation = Reservation.builder()
            .id(reservationId)
            .userId(userId)
            .status(ReservationStatus.CANCEL_CONFIRMED)
            .build();
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.of(reservation));
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            reservationService.cancel(reservationId, userId)
        );
        
        assertEquals(ErrorCode.ALREADY_CANCELED_RESERVATION, exception.getErrorCode());
        verify(redisSeatService, never()).safeIncrement(anyLong(), anyInt());
        verify(redisReservationService, never()).removeFromPendingExpirationQueue(anyLong());
    }
    
    @Test
    @DisplayName("다른 사용자의 예약 취소 시도")
    void cancel_Unauthorized() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long otherUserId = 2L;
        
        Reservation reservation = Reservation.builder()
            .id(reservationId)
            .userId(otherUserId)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.of(reservation));
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            reservationService.cancel(reservationId, userId)
        );
        
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(redisSeatService, never()).safeIncrement(anyLong(), anyInt());
        verify(redisReservationService, never()).removeFromPendingExpirationQueue(anyLong());
    }
    
    @Test
    @DisplayName("존재하지 않는 예약 취소 시도")
    void cancel_ReservationNotFound() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.empty());
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            reservationService.cancel(reservationId, userId)
        );
        
        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
        verify(redisSeatService, never()).safeIncrement(anyLong(), anyInt());
        verify(redisReservationService, never()).removeFromPendingExpirationQueue(anyLong());
    }
    
    @Test
    @DisplayName("결제 완료된 예약 취소 시도")
    void cancel_PaymentConfirmed() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long scheduleId = 1L;
        int quantity = 2;
        
        Reservation reservation = Reservation.builder()
            .id(reservationId)
            .userId(userId)
            .scheduleId(scheduleId)
            .quantity(quantity)
            .status(ReservationStatus.PAYMENTS_CONFIRMED)
            .build();
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.of(reservation));
        
        reservationService.cancel(reservationId, userId);
        
        // then
        assertEquals(ReservationStatus.CANCEL_PENDING, reservation.getStatus());
        verify(redisSeatService).safeIncrement(scheduleId, quantity);
        verify(redisReservationService).removeFromPendingExpirationQueue(reservationId);
    }
} 