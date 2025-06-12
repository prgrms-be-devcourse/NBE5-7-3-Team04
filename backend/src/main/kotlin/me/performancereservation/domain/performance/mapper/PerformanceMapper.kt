package me.performancereservation.domain.performance.mapper

import me.performancereservation.domain.performance.dto.performance.*
import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleResponse
import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.entities.PerformanceSchedule
import me.performancereservation.domain.performance.enums.PerformanceCategory
import me.performancereservation.domain.performance.enums.PerformanceStatus

object PerformanceMapper {
    /** 요청 객체 엔티티 변환
     *
     * @param request
     * @return Performance
     */
    @JvmStatic
    fun toEntity(request: PerformanceCreateRequest, managerId: Long): Performance {
        return Performance(
            fileId = request.fileId,
            managerId = managerId,
            title = request.title,
            venue = request.venue,
            price = request.price,
            totalSeats = request.totalSeats,
            category = PerformanceCategory.valueOf(request.category),
            startDate = request.startDate,
            endDate = request.endDate,
            description = request.description,
            status = PerformanceStatus.PENDING)

    }

    /** 공연 상세 페이지용 응답
     *
     * @param performance
     * @param fileUrl
     * @param schedules
     * @return PerformanceDetailResponse
     */
    @JvmStatic
    fun toDetailResponse(
        performance: Performance,
        fileUrl: String,
        bookmarked: Boolean,
        schedules: List<PerformanceSchedule>
    ): PerformanceDetailResponse {
        return PerformanceDetailResponse(
            id = performance.id!!,
            title = performance.title,
            price = performance.price,
            totalSeats = performance.totalSeats,
            venue = performance.venue,
            description = performance.description,
            status = performance.status,
            fileUrl = fileUrl,
            startDate = performance.startDate,
            endDate = performance.endDate,
            bookmarked = bookmarked,
            schedules = schedules.stream().map { schedule: PerformanceSchedule ->
                PerformanceScheduleMapper.toResponse(schedule)
            }.toList()
        )
    }

    /** 공연 목록 페이지 응답용
     *
     * @param performance
     * @param fileUrl
     * @return PerformanceListResponse
     */
    @JvmStatic
    fun toListResponse(performance: Performance, fileUrl: String?): PerformancePageResponse {
        return PerformancePageResponse(
            id = performance.id!!,
            fileUrl = fileUrl,
            title = performance.title,
            price = performance.price,
            startDate = performance.startDate,
            endDate = performance.endDate,
            venue = performance.venue,
            category = performance.category
        )
    }

    /** 공연자 공연 목록 페이지 응답용
     *
     * @param performance
     * @param fileUrl
     * @return PerformanceManagerListResponse
     */
    @JvmStatic
    fun toManagerListResponse(performance: Performance, fileUrl: String): PerformanceManagerPageResponse {
        return PerformanceManagerPageResponse(
            id = performance.id!!,
            fileUrl = fileUrl,
            title = performance.title,
            startDate = performance.startDate,
            endDate = performance.endDate,
            venue = performance.venue,
            status = performance.status,
            category = performance.category
        )
    }

    /** 공연자 공연 관리 페이지 응답용
     *
     * @param performance
     * @param fileUrl
     * @return PerformanceManagerDetailResponse
     */
    @JvmStatic
    fun toManagerDetailResponse(
        performance: Performance,
        fileUrl: String,
        schedules: List<PerformanceScheduleResponse>
    ): PerformanceManagerDetailResponse {
        return PerformanceManagerDetailResponse(
            id =performance.id!!,
            fileUrl = fileUrl,
            title = performance.title,
            venue = performance.venue,
            status = performance.status,
            totalSeats = performance.totalSeats,
            startDate = performance.startDate,
            endDate = performance.endDate,
            description = performance.description,
            category = performance.category,
            schedules = schedules
        )
    }
}
