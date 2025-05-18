package me.performancereservation.domain.reservation.service;

import me.performancereservation.domain.reservation.dto.ReservationResponse;

public interface SeatReservationService {
    ReservationResponse reserve(Long performanceId, Long scheduleId, Long userId, int quantity);

    void cancel(Long scheduleId, Long userId);
}
