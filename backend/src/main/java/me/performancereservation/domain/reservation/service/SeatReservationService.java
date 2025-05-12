package me.performancereservation.domain.reservation.service;

import me.performancereservation.domain.reservation.dto.ReservationResponse;

public interface ReservationService {
    ReservationResponse reserve(Long scheduleId, Long userId, int quantity);
}
