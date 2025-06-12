package me.performancereservation.domain.reservation.service.redis;

import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.refund.RefundService;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.domain.reservation.mapper.ReservationMapper;
import me.performancereservation.domain.ticket.Ticket;
import me.performancereservation.domain.ticket.TicketRepository;
import me.performancereservation.domain.ticket.enums.TicketStatus;
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
import java.util.List;
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

    @Mock
    private RedisReservationCancelExecutor redisReservationCancelExecutor;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private RefundService refundService;
    
    @InjectMocks
    private RedisSeatReservationService reservationService;
    
    @Test
    @DisplayName("정상적인 예약 시도")
    void reserve_Success() {
        // given
        Long performanceId = 1L;
        Long scheduleId = 1L;
        Long userId = 1L;
        int quantity = 2;
        
        SchedulePerformanceInfo scheduleInfo = new SchedulePerformanceInfo(
            performanceId,
            "Test Performance",
            "Test Venue",
            10000,
            scheduleId,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2)
        );
        
        Reservation reservation = Reservation.builder()
            .id(1L)
            .performanceId(performanceId)
            .scheduleId(scheduleId)
            .userId(userId)
            .quantity(quantity)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
        
        ReservationResponse expectedResponse = new ReservationResponse(
            reservation.getId(),
            "Test Performance",
            "Test Venue",
            quantity,
            ReservationStatus.PAYMENTS_PENDING,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(30),
            10000,
            20000,
            List.of()
        );
        
        // when
        when(performanceScheduleRepository.findSchedulePerformanceInfoByScheduleId(scheduleId))
            .thenReturn(Optional.of(scheduleInfo));
        when(reservationRepository.save(any(Reservation.class)))
            .thenReturn(reservation);
        when(reservationMapper.toResponseDto(any(Reservation.class), any(SchedulePerformanceInfo.class)))
            .thenReturn(expectedResponse);
        
        ReservationResponse result = reservationService.reserve(performanceId, scheduleId, userId, quantity);
        
        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(redisSeatService).safeDecrement(scheduleId, quantity);
        verify(redisReservationService).addToPendingExpirationQueue(reservation.getId());
        verify(ticketRepository, times(quantity)).save(any(Ticket.class));
    }
    
    @Test
    @DisplayName("좌석이 부족한 경우 예약 시도")
    void reserve_NoRemainingSeats() {
        // given
        Long performanceId = 1L;
        Long scheduleId = 1L;
        Long userId = 1L;
        int quantity = 2;
        
        // when
        doThrow(ErrorCode.NO_REMAINING_SEATS.domainException("남은 좌석이 없습니다."))
            .when(redisSeatService).safeDecrement(scheduleId, quantity);
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            reservationService.reserve(performanceId, scheduleId, userId, quantity)
        );
        
        assertEquals(ErrorCode.NO_REMAINING_SEATS, exception.getErrorCode());
        assertEquals("남은 좌석이 없습니다.", exception.getMessage());
    }
    
    @Test
    @DisplayName("존재하지 않는 공연 회차에 대한 예약 시도")
    void reserve_ScheduleNotFound() {
        // given
        Long performanceId = 1L;
        Long scheduleId = 1L;
        Long userId = 1L;
        int quantity = 2;
        
        // when
        doThrow(ErrorCode.SEAT_STOCK_DECREMENT_FAILED.serviceException("좌석 차감에 실패했습니다."))
            .when(redisSeatService).safeDecrement(scheduleId, quantity);
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            reservationService.reserve(performanceId, scheduleId, userId, quantity)
        );
        
        assertEquals(ErrorCode.SEAT_STOCK_DECREMENT_FAILED, exception.getErrorCode());
        assertEquals("좌석 차감에 실패했습니다.", exception.getMessage());
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
        when(refundService.getRefundIdByUserId(userId, reservationId))
            .thenReturn(1L);
        
        Long refundId = reservationService.cancel(reservationId, userId);
        
        // then
        assertEquals(1L, refundId);
        verify(redisReservationCancelExecutor).executeForUserCancel(reservation);
        verify(refundService, times(2)).getRefundIdByUserId(userId, reservationId);
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
        assertEquals("이미 취소된 예약입니다.", exception.getMessage());
        verify(redisReservationCancelExecutor, never()).executeForUserCancel(any(Reservation.class));
        verify(refundService, never()).getRefundIdByUserId(anyLong(), anyLong());
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
        
        assertEquals(ErrorCode.PERMISSION_DENIED, exception.getErrorCode());
        assertEquals("접근 권한이 없습니다.", exception.getMessage());
        verify(redisReservationCancelExecutor, never()).executeForUserCancel(any(Reservation.class));
        verify(refundService, never()).getRefundIdByUserId(anyLong(), anyLong());
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
        assertEquals("해당하는 예약이 없습니다.", exception.getMessage());
    }
}