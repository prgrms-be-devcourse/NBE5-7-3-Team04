package me.performancereservation.domain.reservation.dto

import me.performancereservation.domain.reservation.enums.ReservationStatus
import java.time.LocalDateTime

/**
 * 예약 목록 조회용 응답 Dto
 */
data class ReservationPageResponse(
    // Reservation
    val reservationId: Long,  // 예약 ID
    val quantity: Int,  // 티켓 수량
    val status: ReservationStatus,  // 예약 상태
    val createdAt: LocalDateTime,  // 예약 일시

    // Performance
    val title: String,  // 공역 제목
    val venue: String,  // 공연 장소
    val ticketPrice: Int,  // 티켓 가격

    // Calculated value
    val totalPrice: Int // 총 결제 금액
)
