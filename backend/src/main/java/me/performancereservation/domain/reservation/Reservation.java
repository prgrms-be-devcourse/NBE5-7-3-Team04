package me.performancereservation.domain.reservation;

import jakarta.persistence.*;
import lombok.*;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.reservation.enums.ReservationStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 예약 ID

    private Long userId; // (FK) 유저 ID

    private Long scheduleId; // (FK) 공연회차 ID

    private int quantity; // 티켓 수량

    @Enumerated(EnumType.STRING)
    private ReservationStatus status; //예약 상태 (예약 or 취소)

    public void setStatus(ReservationStatus status) {
        // 유효성검사
        this.status = status;
    }

    @Builder
    public Reservation(Long id, Long userId, Long scheduleId, int quantity, ReservationStatus status) {
        this.id = id;
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.quantity = quantity;
        this.status = status;
    }
}
