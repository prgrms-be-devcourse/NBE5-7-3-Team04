package me.performancereservation.domain.performance.mapper

import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleRequest
import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleResponse
import me.performancereservation.domain.performance.entities.PerformanceSchedule

object PerformanceScheduleMapper {
    /** 요청 객체 엔티티 변환
     *
     * @param request
     * @param performanceId
     * @return PerformanceSchedule
     */
    fun toEntity(request: PerformanceScheduleRequest, performanceId: Long, totalSeats: Int): PerformanceSchedule {
        return PerformanceSchedule(
            performanceId = performanceId,
            startTime = request.startTime,
            endTime = request.endTime,
            remainingSeats = totalSeats,
            canceled = false
        )
    }

    /** 회차 정보 응답 객체 변환
     *
     * @param schedule
     * @return PerformanceScheduleResponse
     */
    fun toResponse(schedule: PerformanceSchedule): PerformanceScheduleResponse {
        return PerformanceScheduleResponse(
            schedule.id!!,
            schedule.startTime,
            schedule.endTime,
            schedule.remainingSeats,
            schedule.canceled
        )
    }
}
