package me.performancereservation.domain.performance.dto.performance

import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleResponse
import me.performancereservation.domain.performance.enums.PerformanceCategory
import me.performancereservation.domain.performance.enums.PerformanceStatus
import java.time.LocalDateTime

/** 상세 페이지 응답용
 *
 * @param id
 * @param title
 * @param price
 * @param totalSeats
 * @param venue
 * @param description
 * @param status        // 판매중 여부 (공연 예약이 가능한 상태인지)
 * @param fileUrl
 * @param schedules     // 회차 정보
 */

data class PerformanceDetailResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val totalSeats: Int,
    val venue: String,
    val description: String,
    val status: PerformanceStatus,
    val fileUrl: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val bookmarked: Boolean,
    val schedules: List<PerformanceScheduleResponse>
)

/** 공연 관리자 자신의 공연 관리 상세 페이지 응답용
 *
 * @param id
 * @param fileUrl
 * @param title
 * @param venue
 * @param status
 * @param totalSeats
 * @param schedules
 */
data class PerformanceManagerDetailResponse(
    val id: Long,
    val fileUrl: String?,
    val title: String,
    val venue: String,
    val status: PerformanceStatus,
    val totalSeats: Int,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val description: String,
    val category: PerformanceCategory,
    val schedules: List<PerformanceScheduleResponse>
)

/** 공연 관리자 공연 목록 페이지 응답용
 *
 * @param id
 * @param fileUrl
 * @param title
 * @param startDate
 * @param endDate
 * @param venue
 * @param status    // 공연 등록 여부 (PENDING, CONFIRM 등)
 */

data class PerformanceManagerPageResponse(
    val id: Long,
    val fileUrl: String?,
    val title: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val venue: String,
    val status: PerformanceStatus,
    val category: PerformanceCategory
)

/** 고객 공연 목록 페이지 응답용
 *
 * @param id
 * @param fileUrl   // 썸네일 파일 주소
 * @param title
 * @param price
 * @param startDate
 * @param endDate
 * @param venue
 * @param category
 */
data class PerformancePageResponse(
    val id: Long,
    val fileUrl: String?,
    val title: String,
    val price: Int,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val venue: String,
    val category: PerformanceCategory
)

