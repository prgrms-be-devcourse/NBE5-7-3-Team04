package me.performancereservation.domain.performance.mapper;

import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleRequest;
import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleResponse;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;

public class PerformanceScheduleMapper {
    /** 요청 객체 엔티티 변환
     *
     * @param request
     * @param performanceId
     * @return PerformanceSchedule
     */
    public static PerformanceSchedule toEntity(PerformanceScheduleRequest request, Long performanceId, int totalSeats) {
        return PerformanceSchedule.builder()
                .performanceId(performanceId)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .remainingSeats(totalSeats)
                .canceled(false)
                .build();
    }

    /** 회차 정보 응답 객체 변환
     *
     * @param schedule
     * @return PerformanceScheduleResponse
     */
    public static PerformanceScheduleResponse toResponse(PerformanceSchedule schedule) {
        return new PerformanceScheduleResponse(
                schedule.getId(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRemainingSeats(),
                schedule.isCanceled()
        );
    }
}
