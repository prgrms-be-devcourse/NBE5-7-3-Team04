package me.performancereservation.domain.reservation.dto

import me.performancereservation.domain.reservation.enums.ReservationStatus
import java.time.LocalDateTime

/**
 * 예약 정보 응답 Dto
 */
data class ReservationResponse(
    val reservationId: Long,  // 공연 예약 ID
    val title: String,  // 공연 제목
    val venue: String,  // 공연 위치
    val quantity: Int,  // 티켓 수량
    val status: ReservationStatus,  // 예약 상태
    val createdAt: LocalDateTime,  // 예약 신청 일시
    val expirationAt: LocalDateTime,  // 결제 만료 일시
    val ticketPrice: Int,  // 티켓 가격
    val totalPrice: Int,  // 총 결제 금액
    val ticketNumbers: List<String> // 티켓 번호 목록
) 