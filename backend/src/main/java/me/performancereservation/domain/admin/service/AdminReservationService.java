package me.performancereservation.domain.admin.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.admin.dto.AdminReservationPageResponse;
import me.performancereservation.domain.admin.repository.AdminReservationRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminReservationService {
    private final ReservationRepository reservationRepository;
    private final AdminReservationRepository adminReservationRepository;

    @Transactional(readOnly = true)
    public Page<AdminReservationPageResponse> getReservationList(Pageable pageable) {
        return adminReservationRepository.findAdminReservations(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AdminReservationPageResponse> searchReservationList(Pageable pageable,
                                                                    String userName,
                                                                    String performanceTitle,
                                                                    ReservationStatus reservationStatus,
                                                                    LocalDateTime startDate,
                                                                    LocalDateTime endDate) {
        return adminReservationRepository.searchAdminReservations(userName, performanceTitle, reservationStatus, startDate, endDate, pageable);
    }

    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ErrorCode.RESERVATION_NOT_FOUND.domainException("예약이 존재하지 않습니다."));

        reservation.confirm();
    }

}
