package me.performancereservation.domain.refund.dto

import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.refund.Refund
import me.performancereservation.domain.refund.enums.RefundStatus
import java.time.LocalDateTime

data class RefundDetailResponse( // Refund에서 데이터 전달
    val refundId: Long,
    val userId: Long,
    val reservationId: Long,

    // 계좌, 은행, 입금자명은 nullable
    val account: String?,
    val bank: String?,
    val depositorName: String?,

    val refundStatus: RefundStatus,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,  // Reservation에서 가져오는 데이터

    val quantity: Int,  // PerformanceSchedule에서 가져오는 데이터

    val startTime: LocalDateTime,  // Performance에서 가져오는 데이터 (id, totalSeats 외 모두)

    val fileId: Long,  // (FK) 파일 ID - 공연 썸네일 용도
    val title: String,  // 제목
    val venue: String,  // 공연 장소
    val price: Int,  // 가격
    val category: String,  // 공연 분류
    val performanceDate: LocalDateTime,  // 공연 일시
    val description: String // 설명
) {
    companion object {
        fun fromEntity(
            refund: Refund,
            reservationQuantity: Int,
            startTime: LocalDateTime,
            performance: Performance
        ): RefundDetailResponse {
            return RefundDetailResponse(
                refund.id!!,
                refund.userId,
                refund.reservationId,
                refund.account,
                refund.bank,
                refund.depositorName,
                refund.status,
                refund.createdAt,
                refund.updatedAt,

                reservationQuantity,
                startTime,

                performance.fileId!!,
                performance.title,
                performance.venue,
                performance.price,
                performance.category.toString(),
                performance.startDate,
                performance.description
            )
        }
    }
}
