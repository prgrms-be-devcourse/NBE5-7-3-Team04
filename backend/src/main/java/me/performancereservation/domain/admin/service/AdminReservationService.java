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

    /** 관리자 예약 목록 조회
     *
     * @param pageable
     * @return AdminReservationPageResponse
     */
    @Transactional(readOnly = true)
    public Page<AdminReservationPageResponse> getReservationList(Pageable pageable) {
        return adminReservationRepository.findAdminReservations(pageable);
    }

    /** 관리자 예약 목록 검색
     *
     * @param pageable 페이징
     * @param userName 회원 이름(검색 키)
     * @param performanceTitle 공연 제목(검색 키)
     * @param reservationStatus 예약 상태(검색 키)
     * @param startDate 시작 날짜(검색 키)
     * @param endDate 종료 날짜(검색 키)
     * @return AdminReservationPageResponse
     */
    @Transactional(readOnly = true)
    public Page<AdminReservationPageResponse> searchReservationList(Pageable pageable,
                                                                    String userName,
                                                                    String performanceTitle,
                                                                    ReservationStatus reservationStatus,
                                                                    LocalDateTime startDate,
                                                                    LocalDateTime endDate) {
        return adminReservationRepository.searchAdminReservations(userName, performanceTitle, reservationStatus, startDate, endDate, pageable);
    }

    /** 관리자 예약 확정 상태 변경
     *
     * 예약을 확정 상태로 변경
     * @param reservationId 예약 id
     */
    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ErrorCode.RESERVATION_NOT_FOUND.domainException("예약이 존재하지 않습니다."));

        reservation.confirm();
    }

}
