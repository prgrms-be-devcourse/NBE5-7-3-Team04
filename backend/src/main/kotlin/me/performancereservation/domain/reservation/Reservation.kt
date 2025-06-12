package me.performancereservation.domain.reservation

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.reservation.enums.ReservationStatus

@Entity
class Reservation (
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    val id: Long? = null, // (PK) 예약 ID

    val userId: Long, // (FK) 유저 ID

    val performanceId: Long, // (FK) 공연 ID

    val scheduleId: Long, // (FK) 공연회차 ID

    var quantity: Int, // 티켓 수량

    @Enumerated(EnumType.STRING)
    var status: ReservationStatus // 예약 상태 (예약 or 취소)
) : BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Reservation) return false

        if (id == null || other.id == null) return false

        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0

        result = 31 * result + updatedAt.hashCode()

        return result
    }

    // 예약 취소 가능 여부
    val isCancelable: Boolean
        get() = status in listOf(
            ReservationStatus.PAYMENTS_PENDING,
            ReservationStatus.PAYMENTS_CONFIRMED
        )

    // 환불이 필요한 상태인지
    val isRefundRequired: Boolean
        get() = this.status == ReservationStatus.PAYMENTS_CONFIRMED

    // 이미 예약 취소 상태인지
    val isAlreadyCanceled: Boolean
        get() = status in listOf(
            ReservationStatus.CANCEL_CONFIRMED,
            ReservationStatus.CANCEL_PENDING
        )

    // 예약 취소 확정 상태로 변경
    fun cancelConfirm() {
        this.status = ReservationStatus.CANCEL_CONFIRMED
    }

    // 예약 취소 대기 상태로 변경
    fun requestCancel() {
        this.status = ReservationStatus.CANCEL_PENDING
    }

    // 공연 티켓 가격 * 수량 -> 총 가격
    fun calculateTotalPrice(ticketPrice: Int): Int {
        return this.quantity * ticketPrice
    }

    // 예약 확정
    fun confirm() {
        this.status = ReservationStatus.PAYMENTS_CONFIRMED
    }
}
