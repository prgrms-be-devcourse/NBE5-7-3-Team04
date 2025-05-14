package me.performancereservation.domain.reservation.dto;

import me.performancereservation.domain.reservation.enums.ReservationStatus;

import java.time.LocalDateTime;

/**
 * 예약 목록 조회용 응답 Dto
 */
public record ReservationPageResponse(
        // Reservation
        Long reservationId, // 예약 ID
        int quantity, // 티켓 수량
        ReservationStatus status, // 예약 상태
        LocalDateTime createdAt, // 예약 일시

        // Performance
        String title, // 공역 제목
        String venue, // 공연 장소
        int ticketPrice, // 티켓 가격

        // Calculated value
        int totalPrice // 총 결제 금액
) {}
