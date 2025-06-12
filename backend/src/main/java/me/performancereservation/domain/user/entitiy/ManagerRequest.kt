package me.performancereservation.domain.user.entitiy

import jakarta.persistence.*

import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.user.enums.ManagerRequestStatus
import java.time.LocalDate

@Entity
class ManagerRequest(// 매니저 권한 승인 요청 내역 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId: Long,

    val reason: String,                  // 신청 사유

    val experience: String,              // 공연 경험

    val organizationName: String,        // 개인 혹은 단체명

    val organizationContact: String,     // 연락처
    @Column
    var approvedAt: LocalDate? = null,    // 승인 날짜

    @Enumerated(EnumType.STRING)
    var status: ManagerRequestStatus    // 승인 요청 상태
) : BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ManagerRequest) return false
        if (id == null || other.id == null) return false
        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + updatedAt.hashCode()
        return result
    }

    val isPending: Boolean
        get() = status == ManagerRequestStatus.PENDING

    fun approve() {
        status = ManagerRequestStatus.APPROVED
        approvedAt = LocalDate.now()
    }

    fun reject() {
        status = ManagerRequestStatus.REJECTED
    }
}
