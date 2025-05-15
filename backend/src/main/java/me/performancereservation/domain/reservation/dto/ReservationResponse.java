package me.performancereservation.domain.reservation.dto;

import me.performancereservation.domain.reservation.enums.ReservationStatus;

import java.time.LocalDateTime;

/**
 * 예약 정보 응답 Dto
 */
public record ReservationResponse(
        long reservationId, // 공연 예약 ID
        String title, // 공연 제목
        String venue, // 공연 위치
        int quantity, // 티켓 수량
        ReservationStatus status, // 예약 상태
        LocalDateTime createdAt, // 예약 신청 일시
        LocalDateTime expirationAt, // 결제 만료 일시
        int ticketPrice, // 티켓 가격
        int totalPrice // 총 결제 금액
) {}