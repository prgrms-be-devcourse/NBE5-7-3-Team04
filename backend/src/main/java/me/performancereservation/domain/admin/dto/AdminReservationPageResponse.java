package me.performancereservation.domain.admin.dto;

import me.performancereservation.domain.reservation.enums.ReservationStatus;

import java.time.LocalDateTime;

public record AdminReservationPageResponse(
        Long reservationId,
        Long performanceId,
        Long performanceScheduleId,
        String name,
        String title,
        int price,
        int quantity,
        ReservationStatus status,
        LocalDateTime createdAt
) {
}
