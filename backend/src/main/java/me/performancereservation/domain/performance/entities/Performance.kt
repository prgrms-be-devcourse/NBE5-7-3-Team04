package me.performancereservation.domain.performance.entities

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.performance.dto.performance.PerformanceUpdateRequest
import me.performancereservation.domain.performance.enums.PerformanceCategory
import me.performancereservation.domain.performance.enums.PerformanceStatus
import java.time.LocalDateTime

@Entity
class Performance (
    // 공연 ID
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    // (FK) 파일 ID - 공연 썸네일 용도
    var fileId: Long? = null,
    // (FK) 공연관리자 ID
    var managerId: Long? = null,
    // 제목
    var title: String = "",
    // 공연 장소
    var venue: String = "",
    // 가격
    var price: Int = 0,
    // 총 좌석수
    var totalSeats: Int = 0,
    // 공연 분류
    @field:Enumerated(EnumType.STRING)
    var category: PerformanceCategory = PerformanceCategory.ETC,
    // 공연 시작 일시
    var startDate: LocalDateTime = LocalDateTime.now(),
    // 공연 종료 일시
    var endDate: LocalDateTime = LocalDateTime.now(),
    // 설명
    var description: String = "",
    // 공연 상태
    @field:Enumerated(EnumType.STRING)
    var status: PerformanceStatus = PerformanceStatus.PENDING,
) : BaseEntity() {

    val isPending: Boolean get() = this.status == PerformanceStatus.PENDING

    val isConfirmed: Boolean get() = this.status == PerformanceStatus.CONFIRMED

    fun confirm() {
        this.status = PerformanceStatus.CONFIRMED
    }

    fun reject() {
        this.status = PerformanceStatus.REJECTED
    }

    fun updateFrom(request: PerformanceUpdateRequest) {
        this.fileId = request.fileId
        this.description = request.description
    }

    fun cancel() {
        this.status = PerformanceStatus.CANCELLED
    }

    fun isRegistrationPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Boolean {
        return (!startDate.isBefore(this.startDate) && !endDate.isAfter(this.endDate)) && (startDate.isBefore(endDate))
    }

    fun hasFile(): Boolean {
        return this.fileId != null
    }

    fun hasPermission(managerId: Long): Boolean {
        return this.managerId != null && this.managerId == managerId
    }

    fun completePerformance() {
        this.status = PerformanceStatus.COMPLETED
    }
}
