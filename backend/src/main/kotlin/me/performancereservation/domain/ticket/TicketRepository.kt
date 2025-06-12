package me.performancereservation.domain.ticket

import org.springframework.data.jpa.repository.JpaRepository

interface TicketRepository : JpaRepository<Ticket, Long> {
    fun findAllByReservationId(reservationId: Long): List<Ticket>

    fun findAllByPerformanceId(performanceId: Long): List<Ticket>
}
