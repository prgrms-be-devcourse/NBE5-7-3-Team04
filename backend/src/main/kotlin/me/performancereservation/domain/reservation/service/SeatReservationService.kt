package me.performancereservation.domain.reservation.service

import me.performancereservation.domain.reservation.dto.ReservationResponse

interface SeatReservationService {
    fun reserve(performanceId: Long, scheduleId: Long, userId: Long, quantity: Int): ReservationResponse

    fun cancel(reservationId: Long, userId: Long): Long
}
