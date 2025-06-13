package me.performancereservation.domain.performance.entities

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.global.exception.ErrorCode
import java.time.LocalDateTime

@Entity
class PerformanceSchedule (
    // (PK) 회차 ID
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // (FK) 공연 ID
    var performanceId: Long,
    // 공연 시작 시간
    var startTime: LocalDateTime,
    // 공연 종료 시간
    var endTime: LocalDateTime,
    // 남은 좌석
    var remainingSeats: Int,
    // 회차 취소 여부
    @field:Column(name = "is_canceled")
    var canceled: Boolean
) : BaseEntity() {

    fun cancel() {
        this.canceled = true
    }

    fun decreaseRemainingSeats(quantity: Int) {
        if (this.remainingSeats < quantity) {
            throw ErrorCode.NO_REMAINING_SEATS.domainException("잔여 좌석이 부족합니다. remainingSeats = " + this.remainingSeats)
        }
        this.remainingSeats -= quantity
    }

    fun increaseRemainingSeats(quantity: Int) {
        if (quantity <= 0) {
            throw ErrorCode.NO_REMAINING_SEATS.domainException("좌석수가 1이상이 아님")
        }

        this.remainingSeats += quantity
    }

    fun hasPermission(performanceId: Long?): Boolean {
        return performanceId != null && performanceId == this.performanceId
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is PerformanceSchedule) return false

        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()

        result = 31 * result + updatedAt.hashCode()

        return result
    }
}