package me.performancereservation.domain.refund

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.refund.enums.RefundStatus

@Entity
class Refund ( // account, bank, depositorName는 처음 생성할 때 null
    // 환불요청정보 id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Long? = null, // (FK) 환불을 요청하는 예약 id

    val reservationId: Long, // (FK) 환불을 요청한 유저의 id
    val userId: Long, // 환불대기, 환불완료 상태 표시

    @Enumerated(EnumType.STRING)
    var status: RefundStatus

) : BaseEntity() {
    var account: String? = null // 환불 받을 계좌번호

    var bank: String? = null // 환불 받을 은행

    var depositorName: String? = null // 입금자명

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Refund) return false

        if (id == null || other.id == null) return false

        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0

        result = 31 * result + updatedAt.hashCode()

        return result
    }

    fun updateBankInfo(account: String?, bank: String?, depositorName: String?) {
        this.account = account
        this.bank = bank
        this.depositorName = depositorName
    }

    fun ready() {
        this.status = RefundStatus.READY
    }

    fun confirm() {
        this.status = RefundStatus.CONFIRMED
    }
}
