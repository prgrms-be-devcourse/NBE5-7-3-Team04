package me.performancereservation.domain.user.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.user.enums.ManagerRequestStatus
import java.time.LocalDate

@Entity
class ManagerRequest(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,                // 매니저 권한 승인 요청 내역 ID

    val userId: Long,                    // (FK) 유저 ID

    val organizationName: String,        // 개인 혹은 단체명
    val organizationContact: String,     // 연락처
    val experience: String,              // 공연 경험
    val reason: String,                  // 신청 사유

    @Enumerated(EnumType.STRING)
    var status: ManagerRequestStatus = ManagerRequestStatus.PENDING, // 승인 요청 상태

    var approvedAt: LocalDate? = null // 승인 날짜

) : BaseEntity() {

    fun isPending(): Boolean {
        return status == ManagerRequestStatus.PENDING
    }

    fun approve() {
        status = ManagerRequestStatus.APPROVED
        approvedAt = LocalDate.now()
    }

    fun reject() {
        status = ManagerRequestStatus.REJECTED
    }
}