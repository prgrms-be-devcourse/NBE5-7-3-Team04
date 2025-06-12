package me.performancereservation.domain.refund

import jakarta.persistence.*
import lombok.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.refund.enums.RefundStatus
import me.performancereservation.global.exception.ErrorCode

@Entity
class Refund ( // account, bank, depositorName는 처음 생성할 때 null
    // 환불요청정보 id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Id
    var id: Long? = null, // (FK) 환불을 요청하는 예약 id

    val reservationId: Long, // (FK) 환불을 요청한 유저의 id
    val userId: Long, // 환불대기, 환불완료 상태 표시

    @field:Enumerated(EnumType.STRING)
    @field:Setter
    var status: RefundStatus

) : BaseEntity() {
    var account: String? = null // 환불 받을 계좌번호

    var bank: String? = null // 환불 받을 은행

    var depositorName: String? = null // 입금자명

    fun updateBankInfo(account: String?, bank: String?, depositorName: String?) {
        this.account = account
        this.bank = bank
        this.depositorName = depositorName
    }

    fun ready() {
        this.status = RefundStatus.READY
    }

    fun confirm() {
        if (this.status == RefundStatus.CONFIRMED) {
            throw ErrorCode.INVALID_REFUND_STATUS.domainException("이미 환불이 완료된 상태입니다.")
        }
        this.status = RefundStatus.CONFIRMED
    }
}
