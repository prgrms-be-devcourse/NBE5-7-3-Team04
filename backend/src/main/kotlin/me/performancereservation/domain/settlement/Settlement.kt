package me.performancereservation.domain.settlement

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.settlement.enums.SettlementStatus
import java.time.LocalDateTime

@Entity
class Settlement (
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    var id: Long?, // 정산 ID

    var performanceId: Long, // 공연 ID

    var totalAmount: Int, // 총 정산 금액

    var account: String, // 계좌번호

    var bank: String, // 은행명

    @Enumerated(EnumType.STRING)
    var status: SettlementStatus // 정산 상태
) :
    BaseEntity() {
    var settledAt: LocalDateTime? = null // 정산완료일시. 생성 시 초기값 null

    fun confirm() {
        this.settledAt = LocalDateTime.now()
        this.status = SettlementStatus.CONFIRMED
    }

    fun updateBankInfo(bank: String, account: String): Settlement {
        this.bank = bank
        this.account = account
        return this
    }
}
