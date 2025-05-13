package me.performancereservation.domain.performance.dto.performance.response;

import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleResponse;

import java.util.List;

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
public record PerformanceManagerDetailResponse(
        Long id,
        String fileUrl,
        String title,
        String venue,
        String status,
        int totalSeats,
        List<PerformanceScheduleResponse> schedules
        ) {
}
