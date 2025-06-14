package me.performancereservation.domain.ticket

import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
class TicketService (
    private val ticketRepository: TicketRepository

) {
}
