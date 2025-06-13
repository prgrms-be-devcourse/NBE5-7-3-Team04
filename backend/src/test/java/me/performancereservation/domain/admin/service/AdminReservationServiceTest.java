package me.performancereservation.domain.admin.service;

import me.performancereservation.domain.admin.dto.AdminReservationPageResponse;
import me.performancereservation.domain.admin.repository.AdminReservationRepository;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.domain.sms.SMSService;
import me.performancereservation.domain.user.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    @Mock
    private SMSService smsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private AdminReservationRepository adminReservationRepository;

    @Mock
    private PerformanceScheduleRepository performanceScheduleRepository;

    @InjectMocks
    private AdminReservationService adminReservationService;

    @Test
    @DisplayName("관리자 예약 목록 조회 성공")
    void getReservationList_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        AdminReservationPageResponse expectedResponse = new AdminReservationPageResponse(
                1L,
                1L,
                1L,
                "테스트 사용자",
                "테스트 공연",
                10000,
                2,
                20000,
                ReservationStatus.PAYMENTS_PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Page<AdminReservationPageResponse> expectedPage = new PageImpl<>(List.of(expectedResponse));

        // when
        when(adminReservationRepository.findAdminReservations(pageable))
                .thenReturn(expectedPage);

        Page<AdminReservationPageResponse> result = adminReservationService.getReservationList(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
        verify(adminReservationRepository).findAdminReservations(pageable);
    }

    @Test
    @DisplayName("관리자 예약 목록 검색 성공")
    void searchReservationList_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String userName = "테스트";
        String performanceTitle = "공연";
        ReservationStatus reservationStatus = ReservationStatus.PAYMENTS_PENDING;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        AdminReservationPageResponse expectedResponse = new AdminReservationPageResponse(
                1L,
                1L,
                1L,
                "테스트 사용자",
                "테스트 공연",
                10000,
                2,
                20000,
                ReservationStatus.PAYMENTS_PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Page<AdminReservationPageResponse> expectedPage = new PageImpl<>(List.of(expectedResponse));

        // when
        when(adminReservationRepository.searchAdminReservations(
                userName, performanceTitle, reservationStatus, startDate, endDate, pageable))
                .thenReturn(expectedPage);

        Page<AdminReservationPageResponse> result = adminReservationService.searchReservationList(
                pageable, userName, performanceTitle, reservationStatus, startDate, endDate);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
        verify(adminReservationRepository).searchAdminReservations(
                userName, performanceTitle, reservationStatus, startDate, endDate, pageable);
    }

    @Test
    @DisplayName("예약 확정 성공")
    void confirmReservation_Success() {
        // given
        Long reservationId = 1L;
        Long scheduleId = 1L;
        int quantity = 2;

        Reservation reservation = mock(Reservation.class);
        when(reservation.getScheduleId()).thenReturn(scheduleId);
        when(reservation.getQuantity()).thenReturn(quantity);

        PerformanceSchedule schedule = mock(PerformanceSchedule.class);

        // when
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));
        when(performanceScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.of(schedule));

        adminReservationService.confirmReservation(reservationId);

        // then
        verify(reservationRepository).findById(reservationId);
        verify(performanceScheduleRepository).findById(scheduleId);
        verify(reservation).confirm();
        verify(schedule).decreaseRemainingSeats(quantity);
    }

    @Test
    @DisplayName("존재하지 않는 예약 확정 시도")
    void confirmReservation_ReservationNotFound() {
        // given
        Long reservationId = 1L;

        // when
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminReservationService.confirmReservation(reservationId)
        );

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
        verify(reservationRepository).findById(reservationId);
        verify(performanceScheduleRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 공연 회차로 예약 확정 시도")
    void confirmReservation_ScheduleNotFound() {
        // given
        Long reservationId = 1L;
        Long scheduleId = 1L;

        Reservation reservation = mock(Reservation.class);
        when(reservation.getScheduleId()).thenReturn(scheduleId);

        // when
        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));
        when(performanceScheduleRepository.findById(scheduleId))
                .thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminReservationService.confirmReservation(reservationId)
        );

        assertEquals(ErrorCode.PERFORMANCE_SCHEDULE_NOT_FOUND, exception.getErrorCode());
        verify(reservationRepository).findById(reservationId);
        verify(performanceScheduleRepository).findById(scheduleId);
        verify(reservation, never()).confirm();
    }

    @Test
    @DisplayName("관리자 예약 목록 검색 - null 검색 조건으로 전체 검색")
    void searchReservationList_WithNullConditions() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        AdminReservationPageResponse expectedResponse = new AdminReservationPageResponse(
                1L,
                1L,
                1L,
                "테스트 사용자",
                "테스트 공연",
                10000,
                2,
                20000,
                ReservationStatus.PAYMENTS_CONFIRMED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Page<AdminReservationPageResponse> expectedPage = new PageImpl<>(List.of(expectedResponse));

        // when
        when(adminReservationRepository.searchAdminReservations(
                null, null, null, null, null, pageable))
                .thenReturn(expectedPage);

        Page<AdminReservationPageResponse> result = adminReservationService.searchReservationList(
                pageable, null, null, null, null, null);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));
        verify(adminReservationRepository).searchAdminReservations(
                null, null, null, null, null, pageable);
    }

    @Test
    @DisplayName("빈 예약 목록 조회")
    void getReservationList_EmptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminReservationPageResponse> emptyPage = new PageImpl<>(List.of());

        // when
        when(adminReservationRepository.findAdminReservations(pageable))
                .thenReturn(emptyPage);

        Page<AdminReservationPageResponse> result = adminReservationService.getReservationList(pageable);

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(adminReservationRepository).findAdminReservations(pageable);
    }
}