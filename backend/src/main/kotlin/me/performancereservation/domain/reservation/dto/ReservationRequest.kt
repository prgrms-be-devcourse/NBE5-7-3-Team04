package me.performancereservation.domain.reservation.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 유저의 예약 시도를 위한 요청 Dto
 */
data class ReservationRequest(
    @field:NotNull
    val performanceId: Long,  // 공연 ID

    @field:NotNull
    val scheduleId: Long,  // 공연 회차 ID

    @field:NotNull
    @field:Positive
    val quantity: Int // 티켓 수량
) 