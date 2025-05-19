package me.performancereservation.domain.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByReservationId(Long reservationId);

    List<Ticket> findAllByPerformanceId(Long performanceId);
}
