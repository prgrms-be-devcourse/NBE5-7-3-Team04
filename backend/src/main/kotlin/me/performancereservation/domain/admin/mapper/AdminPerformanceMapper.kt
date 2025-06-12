package me.performancereservation.domain.admin.mapper

import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse
import me.performancereservation.domain.admin.dto.response.PendingPerformanceScheduleResponse
import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.entities.PerformanceSchedule

object AdminPerformanceMapper {
    /**
     * 관리자 공연 Pending 목록 페이지 응답용
     *
     * @param performance 공연 엔티티
     * @param fileUrl 파일 URL
     * @param managerName 매니저 이름
     * @param schedules 공연 스케줄 응답 목록
     * @return PendingPerformancePageResponse
     */
    @JvmStatic  //TODO: 서비스 코틀린으로 변환시 @JvmStatic 제거 필요
    fun toPendingResponse(
        performance: Performance,
        fileUrl: String?,
        managerName: String,
        schedules: List<PendingPerformanceScheduleResponse>
    ): PendingPerformancePageResponse {
        return PendingPerformancePageResponse(
            id = performance.id!!,
            fileUrl = fileUrl,
            performanceManagerName = managerName,
            title = performance.title,
            venue = performance.venue,
            price = performance.price,
            totalSeats = performance.totalSeats,
            category = performance.category,
            status = performance.status,
            startDate = performance.startDate,
            endDate = performance.endDate,
            description = performance.description,
            schedules = schedules
        )
    }

    /**
     * 공연 스케줄을 PendingPerformanceScheduleResponse로 변환
     *
     * @param schedule 공연 스케줄 엔티티
     * @return PendingPerformanceScheduleResponse
     */
    @JvmStatic  //TODO: 서비스 코틀린으로 변환시 @JvmStatic 제거 필요
    fun toScheduleResponse(schedule: PerformanceSchedule): PendingPerformanceScheduleResponse {
        return PendingPerformanceScheduleResponse(
            id = schedule.id!!,
            startTime = schedule.startTime,
            endTime = schedule.endTime
        )
    }
}
