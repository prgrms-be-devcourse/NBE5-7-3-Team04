package me.performancereservation.domain.reservation.service;

import me.performancereservation.domain.file.FileRepository;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse;
import me.performancereservation.domain.reservation.dto.ReservationPageResponse;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.domain.reservation.mapper.ReservationMapper;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationQueryServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private PerformanceScheduleRepository performanceScheduleRepository;
    
    @Mock
    private PerformanceRepository performanceRepository;
    
    @Mock
    private FileRepository fileRepository;
    
    @Mock
    private ReservationMapper reservationMapper;
    
    @InjectMocks
    private ReservationQueryService queryService;
    
    @Test
    @DisplayName("사용자의 모든 예약 목록 조회")
    void getAllByUserId_Success() {
        // given
        Long userId = 1L;
        Long scheduleId = 1L;
        int quantity = 2;
        Pageable pageable = PageRequest.of(0, 10);
        
        Reservation reservation = Reservation.builder()
            .id(1L)
            .userId(userId)
            .scheduleId(scheduleId)
            .quantity(quantity)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
        
        SchedulePerformanceInfo scheduleInfo = new SchedulePerformanceInfo(
            1L,
            "Test Performance",
            "Test Venue",
            10000,
            scheduleId,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2)
        );
        
        ReservationPageResponse expectedResponse = new ReservationPageResponse(
            1L,
            quantity,
            ReservationStatus.PAYMENTS_PENDING,
            LocalDateTime.now(),
            "Test Performance",
            "Test Venue",
            10000,
            20000
        );
        
        // when
        when(reservationRepository.findAllByUserId(userId, pageable))
            .thenReturn(new PageImpl<>(List.of(reservation), pageable, 1));
        when(performanceScheduleRepository.findAllSchedulePerformanceInfoByScheduleIds(List.of(scheduleId)))
            .thenReturn(List.of(scheduleInfo));
        when(reservationMapper.toListResponseDto(any(Reservation.class), any(SchedulePerformanceInfo.class)))
            .thenReturn(expectedResponse);
        
        Page<ReservationPageResponse> result = queryService.getAllByUserId(userId, pageable);
        
        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
    }
    
    @Test
    @DisplayName("존재하지 않는 예약 상세 조회")
    void getByReservationId_NotFound() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.empty());
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            queryService.getByReservationId(reservationId, userId)
        );
        
        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
        assertEquals("해당하는 예약이 없습니다.", exception.getMessage());
    }
    
    @Test
    @DisplayName("다른 사용자의 예약 상세 조회")
    void getByReservationId_Unauthorized() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long otherUserId = 2L;
        
        Reservation reservation = Reservation.builder()
            .id(reservationId)
            .userId(otherUserId)
            .scheduleId(1L)
            .quantity(2)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.of(reservation));
        
        // then
        AppException exception = assertThrows(AppException.class, () -> 
            queryService.getByReservationId(reservationId, userId)
        );
        
        assertEquals(ErrorCode.PERMISSION_DENIED, exception.getErrorCode());
        assertEquals("접근 권한이 없습니다.", exception.getMessage());
    }
    
    @Test
    @DisplayName("정상적인 예약 상세 조회")
    void getByReservationId_Success() {
        // given
        Long reservationId = 1L;
        Long userId = 1L;
        Long scheduleId = 1L;
        Long performanceId = 1L;
        int quantity = 2;
        
        Reservation reservation = Reservation.builder()
            .id(reservationId)
            .userId(userId)
            .performanceId(performanceId)
            .scheduleId(scheduleId)
            .quantity(quantity)
            .status(ReservationStatus.PAYMENTS_PENDING)
            .build();
        
        Performance performance = Performance.builder()
            .id(performanceId)
            .title("Test Performance")
            .venue("Test Venue")
            .description("Test Description")
            .build();
        
        SchedulePerformanceInfo scheduleInfo = new SchedulePerformanceInfo(
            performanceId,
            "Test Performance",
            "Test Venue",
            10000,
            scheduleId,
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2)
        );
        
        ReservationDetailResponse expectedResponse = new ReservationDetailResponse(
            reservationId,
            performanceId,
            "Test Performance",
            "Test Description",
            "Test Venue",
            null,
            quantity,
            ReservationStatus.PAYMENTS_PENDING,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(30),
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(2),
            10000,
            20000,
            List.of()
        );
        
        // when
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.of(reservation));
        when(performanceRepository.findById(performanceId))
            .thenReturn(Optional.of(performance));
        when(performanceScheduleRepository.findSchedulePerformanceInfoByScheduleId(scheduleId))
            .thenReturn(Optional.of(scheduleInfo));
        when(reservationMapper.toDetailResponseDto(any(Reservation.class), any(SchedulePerformanceInfo.class), any(Performance.class), any()))
            .thenReturn(expectedResponse);

        ReservationDetailResponse result = queryService.getByReservationId(reservationId, userId);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(reservationId, result.reservationId());
        assertEquals("Test Performance", result.title());
        assertEquals("Test Venue", result.venue());
        assertEquals(quantity, result.quantity());
        assertEquals(ReservationStatus.PAYMENTS_PENDING, result.status());
        assertEquals(10000, result.ticketPrice());
        assertEquals(20000, result.totalPrice());
    }
} 