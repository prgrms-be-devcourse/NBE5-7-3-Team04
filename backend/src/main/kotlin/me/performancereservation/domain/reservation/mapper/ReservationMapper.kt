package me.performancereservation.domain.reservation.mapper

import lombok.RequiredArgsConstructor
import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo
import me.performancereservation.domain.reservation.Reservation
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse
import me.performancereservation.domain.reservation.dto.ReservationPageResponse
import me.performancereservation.domain.reservation.dto.ReservationResponse
import me.performancereservation.domain.ticket.Ticket
import me.performancereservation.domain.ticket.TicketRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ReservationMapper(
        private val ticketRepository: TicketRepository,
        @Value("\${reservation.expired_time}")
        private val EXPIRE_MINUTES: Long
    ) {


    /**
     * 예약 객체와 공연+공연회차 데이터 모델 객체를 이용해 예약 응답 Dto 생성
     *
     * @param reservation 예약 객체
     * @param schedulePerformanceInfo 공연+공연회차 데이터 모델 객체
     * @return ReservationResponse 예약 응답 dto
     */
    fun toResponseDto(reservation: Reservation, schedulePerformanceInfo: SchedulePerformanceInfo): ReservationResponse {
        val createdAt = reservation.createdAt
        val expirationAt = createdAt.plusMinutes(EXPIRE_MINUTES.toLong())

        val totalPrice = reservation.calculateTotalPrice(schedulePerformanceInfo.price)

        // 관련 티켓들 조회
        val tickets = ticketRepository.findAllByReservationId(reservation.id)

        // 티켓 id 목록 생성
        val ticketNumbers = tickets.stream()
            .map { "${reservation.id}-${it.id}" }
            .toList()

        return ReservationResponse(
            reservation.id!!,
            schedulePerformanceInfo.title,
            schedulePerformanceInfo.venue,
            reservation.quantity,
            reservation.status,
            createdAt,
            expirationAt,
            schedulePerformanceInfo.price,
            totalPrice,
            ticketNumbers
        )
    }

    /**
     * 예약 객체와 공연+공연회차 데이터 모델 객체를 이용해 예약 응답 Dto 생성
     *
     * @param reservation 예약 객체
     * @param schedulePerformanceInfo 공연+공연회차 데이터 모델 객체
     * @return ReservationResponse 예약 응답 dto
     */
    fun toDetailResponseDto(
        reservation: Reservation,
        schedulePerformanceInfo: SchedulePerformanceInfo,
        performance: Performance,
        fileUrl: String
    ): ReservationDetailResponse {
        val createdAt = reservation.createdAt
        val expirationAt = createdAt.plusMinutes(EXPIRE_MINUTES.toLong())

        val totalPrice = reservation.calculateTotalPrice(schedulePerformanceInfo.price)

        // 관련 티켓들 조회
        val tickets = ticketRepository.findAllByReservationId(reservation.id)

        // 티켓 id 목록 생성
        val ticketNumbers = tickets.stream()
            .map { "${reservation.id}-${it.id}" }
            .toList()

        return ReservationDetailResponse(
            reservation.id!!,
            reservation.performanceId,
            schedulePerformanceInfo.title,
            performance.description,
            schedulePerformanceInfo.venue,
            fileUrl,
            reservation.quantity,
            reservation.status,
            createdAt,
            expirationAt,
            schedulePerformanceInfo.startTime,
            schedulePerformanceInfo.endTime,
            schedulePerformanceInfo.price,
            totalPrice,
            ticketNumbers
        )
    }

    /**
     * 예약 객체와 공연+공연회차 데이터 모델 객체를 이용해 예약 응답 Dto Page 생성
     *
     * @param reservation 예약 객체
     * @param schedulePerformanceInfo 공연+공연회차 데이터 모델 객체
     * @return ReservationListResponse 예약 목록 응답 dto
     */
    fun toListResponseDto(
        reservation: Reservation,
        schedulePerformanceInfo: SchedulePerformanceInfo
    ): ReservationPageResponse {
        val createdAt = reservation.createdAt

        val totalPrice = reservation.calculateTotalPrice(schedulePerformanceInfo.price)

        return ReservationPageResponse(
            reservation.id!!,
            reservation.quantity,
            reservation.status,
            createdAt,
            schedulePerformanceInfo.title,
            schedulePerformanceInfo.venue,
            schedulePerformanceInfo.price,
            totalPrice
        )
    }
}