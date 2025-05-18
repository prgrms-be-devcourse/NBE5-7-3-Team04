package me.performancereservation.domain.ticket;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.ticket.enums.TicketStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 티켓 id

    private Long reservationId; // 예약 id

    private Long performanceId; // 공연 id

    @Enumerated(EnumType.STRING)
    private TicketStatus ticketStatus; // 티켓 상태

    @Builder
    public Ticket(Long id, Long reservationId, Long performanceId, TicketStatus ticketStatus) {
        this.id = id;
        this.reservationId = reservationId;
        this.performanceId = performanceId;
        this.ticketStatus = ticketStatus;
    }

    public void cancel(){
        this.ticketStatus = TicketStatus.CANCELLED;
    }

    public void use(){
        this.ticketStatus = TicketStatus.USED;
    }

    public void expire(){
        this.ticketStatus = TicketStatus.EXPIRED;
    }
}
