package me.performancereservation.domain.reservation;

import me.performancereservation.domain.reservation.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    /**
     * 예약 id들과 예약 상태를 조건으로 해당하는 예약들을 조회
     */
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.id IN :ids AND r.status = :status
    """)
    List<Reservation> findAllByIdsAndStatus(@Param("ids") List<Long> ids, @Param("status") ReservationStatus status);
}
