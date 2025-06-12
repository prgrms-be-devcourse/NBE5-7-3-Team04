//package me.performancereservation.domain.reservation.service;
//
//import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
//import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
//import me.performancereservation.domain.reservation.Reservation;
//import me.performancereservation.domain.reservation.ReservationRepository;
//import me.performancereservation.domain.reservation.dto.ReservationDetailResponse;
//import me.performancereservation.domain.reservation.dto.ReservationPageResponse;
//import me.performancereservation.domain.reservation.dto.ReservationResponse;
//import me.performancereservation.domain.reservation.enums.ReservationStatus;
//import me.performancereservation.domain.reservation.mapper.ReservationMapper;
//import me.performancereservation.global.exception.AppException;
//import me.performancereservation.global.exception.ErrorCode;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ReservationQueryServiceTest {
//    @Mock
//    private ReservationRepository reservationRepository;
//
//    @Mock
//    private PerformanceScheduleRepository performanceScheduleRepository;
//
//    @Mock
//    private ReservationMapper reservationMapper;
//
//    @InjectMocks
//    private ReservationQueryService queryService;
//
//    @Test
//    @DisplayName("사용자의 모든 예약 목록 조회")
//    void getAllByUserId_Success() {
//        // given
//        Long userId = 1L;
//        Long scheduleId = 1L;
//        int quantity = 2;
//        Pageable pageable = PageRequest.of(0, 10);
//
//        Reservation reservation = Reservation.builder()
//            .id(1L)
//            .userId(userId)
//            .scheduleId(scheduleId)
//            .quantity(quantity)
//            .status(ReservationStatus.PAYMENTS_PENDING)
//            .build();
//
//        SchedulePerformanceInfo scheduleInfo = new SchedulePerformanceInfo(
//            1L,
//            "Test Performance",
//            "Test Venue",
//            10000,
//            scheduleId,
//            LocalDateTime.now(),
//            LocalDateTime.now().plusHours(2)
//        );
//
//        ReservationPageResponse expectedResponse = new ReservationPageResponse(
//            1L,
//            quantity,
//            ReservationStatus.PAYMENTS_PENDING,
//            LocalDateTime.now(),
//            "Test Performance",
//            "Test Venue",
//            10000,
//            20000
//        );
//
//        // when
//        when(reservationRepository.findAllByUserId(userId, pageable))
//            .thenReturn(new PageImpl<>(List.of(reservation)));
//        when(performanceScheduleRepository.findAllSchedulePerformanceInfoByScheduleIds(List.of(scheduleId)))
//            .thenReturn(List.of(scheduleInfo));
//        when(reservationMapper.toListResponseDto(any(Reservation.class), any(SchedulePerformanceInfo.class)))
//            .thenReturn(expectedResponse);
//
//        Page<ReservationPageResponse> result = queryService.getAllByUserId(userId, pageable);
//
//        // then
//        assertNotNull(result);
//        assertEquals(1, result.getTotalElements());
//        assertEquals(expectedResponse, result.getContent().get(0));
//        verify(reservationRepository).findAllByUserId(userId, pageable);
//        verify(performanceScheduleRepository).findAllSchedulePerformanceInfoByScheduleIds(List.of(scheduleId));
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 예약 상세 조회")
//    void getByReservationId_NotFound() {
//        // given
//        Long reservationId = 1L;
//        Long userId = 1L;
//
//        // when
//        when(reservationRepository.findById(reservationId))
//            .thenReturn(Optional.empty());
//
//        // then
//        AppException exception = assertThrows(AppException.class, () ->
//            queryService.getByReservationId(reservationId, userId)
//        );
//
//        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
//        verify(reservationRepository).findById(reservationId);
//        verify(performanceScheduleRepository, never()).findSchedulePerformanceInfoByScheduleId(anyLong());
//    }
//
//    @Test
//    @DisplayName("다른 사용자의 예약 상세 조회")
//    void getByReservationId_Unauthorized() {
//        // given
//        Long reservationId = 1L;
//        Long userId = 1L;
//        Long otherUserId = 2L;
//
//        Reservation reservation = Reservation.builder()
//            .id(reservationId)
//            .userId(otherUserId)
//            .scheduleId(1L)
//            .quantity(2)
//            .status(ReservationStatus.PAYMENTS_PENDING)
//            .build();
//
//        // when
//        when(reservationRepository.findById(reservationId))
//            .thenReturn(Optional.of(reservation));
//
//        // then
//        AppException exception = assertThrows(AppException.class, () ->
//            queryService.getByReservationId(reservationId, userId)
//        );
//
//        assertEquals(ErrorCode.PERMISSION_DENIED, exception.getErrorCode());
//        verify(reservationRepository).findById(reservationId);
//        verify(performanceScheduleRepository, never()).findSchedulePerformanceInfoByScheduleId(anyLong());
//    }
//
//    @Test
//    @DisplayName("정상적인 예약 상세 조회")
//    void getByReservationId_Success() {
//        // given
//        Long reservationId = 1L;
//        Long userId = 1L;
//        Long scheduleId = 1L;
//        int quantity = 2;
//
//        Reservation reservation = Reservation.builder()
//            .id(reservationId)
//            .userId(userId)
//            .scheduleId(scheduleId)
//            .quantity(quantity)
//            .status(ReservationStatus.PAYMENTS_PENDING)
//            .build();
//
//        SchedulePerformanceInfo scheduleInfo = new SchedulePerformanceInfo(
//            1L,
//            "Test Performance",
//            "Test Venue",
//            10000,
//            scheduleId,
//            LocalDateTime.now(),
//            LocalDateTime.now().plusHours(2)
//        );
//
//        ReservationResponse expectedResponse = new ReservationResponse(
//            reservationId,
//            "Test Performance",
//            "Test Venue",
//            quantity,
//            ReservationStatus.PAYMENTS_PENDING,
//            LocalDateTime.now(),
//            LocalDateTime.now().plusMinutes(30),
//            10000,
//            20000,
//                List.of()
//        );
//
//        // when
//        when(reservationRepository.findById(reservationId))
//            .thenReturn(Optional.of(reservation));
//        when(performanceScheduleRepository.findSchedulePerformanceInfoByScheduleId(scheduleId))
//            .thenReturn(Optional.of(scheduleInfo));
//        when(reservationMapper.toResponseDto(any(Reservation.class), any(SchedulePerformanceInfo.class)))
//            .thenReturn(expectedResponse);
//
//        ReservationDetailResponse result = queryService.getByReservationId(reservationId, userId);
//
//        // then
//        assertNotNull(result);
//        assertEquals(expectedResponse, result);
//        verify(reservationRepository).findById(reservationId);
//        verify(performanceScheduleRepository).findSchedulePerformanceInfoByScheduleId(scheduleId);
//    }
//}