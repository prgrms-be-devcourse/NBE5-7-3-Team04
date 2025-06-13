package me.performancereservation.domain.ticket

import jakarta.persistence.*
import me.performancereservation.domain.ticket.enums.TicketStatus

@Entity
class Ticket (

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    val id: Long? = null, // 티켓 id

    val reservationId: Long, // 예약 id

    val performanceId: Long, // 공연 id

    @Enumerated(EnumType.STRING)
    var ticketStatus: TicketStatus // 티켓 상태
) {
    fun cancel() {
        this.ticketStatus = TicketStatus.CANCELLED
    }

    fun use() {
        this.ticketStatus = TicketStatus.USED
    }

    fun expire() {
        this.ticketStatus = TicketStatus.EXPIRED
    }
}
