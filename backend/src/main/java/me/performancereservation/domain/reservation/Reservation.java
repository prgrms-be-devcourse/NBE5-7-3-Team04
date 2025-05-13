package me.performancereservation.domain.reservation;

import jakarta.persistence.*;
import lombok.*;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.global.exception.ErrorCode;

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
    private ReservationStatus status; // 예약 상태 (예약 or 취소)

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

    /**
     * 도메인 로직
     */

    // 취소 가능한 상태인지
    public boolean isCancelable() {
        return this.status == ReservationStatus.PAYMENTS_PENDING || this.status == ReservationStatus.PAYMENTS_CONFIRMED;
    }

    // 환불이 필요한 상태인지
    public boolean isRefundRequired() {
        return this.status == ReservationStatus.PAYMENTS_CONFIRMED;
    }

    // 이미 예약 취소 상태인지
    public boolean isAlreadyCanceled() {
        return this.status == ReservationStatus.CANCEL_CONFIRMED || this.status == ReservationStatus.CANCEL_PENDING;
    }

    // 예약 취소 확정 상태로 변경
    public void cancelConfirm(){
        if(isAlreadyCanceled()){
            throw ErrorCode.ALREADY_CANCELED_RESERVATION.domainException("이미 취소된 예약임");
        }

        this.status = ReservationStatus.CANCEL_CONFIRMED;
    }

    // 예약 취소 대기 상태로 변경
    public void requestCancel() {
        if(isAlreadyCanceled()){
            throw ErrorCode.ALREADY_CANCELED_RESERVATION.domainException("이미 취소된 예약임");
        }

        this.status = ReservationStatus.CANCEL_PENDING;
    }

    // 공연 티켓 가격 * 수량 -> 총 가격
    public int calculateTotalPrice(int ticketPrice) {
        return this.quantity * ticketPrice;
    }
}
