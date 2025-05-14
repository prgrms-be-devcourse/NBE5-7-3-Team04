package me.performancereservation.domain.admin.mapper;

import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse;
import me.performancereservation.domain.admin.dto.response.PendingPerformanceScheduleResponse;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;

import java.util.List;

public class AdminPerformanceMapper {

    /**
     * 관리자 공연 Pending 목록 페이지 응답용
     *
     * @param performance 공연 엔티티
     * @param fileUrl 파일 URL
     * @param managerName 매니저 이름
     * @param schedules 공연 스케줄 응답 목록
     * @return PendingPerformancePageResponse
     */
    public static PendingPerformancePageResponse toPendingResponse(
            Performance performance,
            String fileUrl,
            String managerName,
            List<PendingPerformanceScheduleResponse> schedules) {

        return new PendingPerformancePageResponse(
                performance.getId(),
                fileUrl,
                managerName,
                performance.getTitle(),
                performance.getVenue(),
                performance.getPrice(),
                performance.getTotalSeats(),
                performance.getCategory(),
                performance.getPerformanceDate(),
                performance.getDescription(),
                schedules
        );
    }

    /**
     * 공연 스케줄을 PendingPerformanceScheduleResponse로 변환
     *
     * @param schedule 공연 스케줄 엔티티
     * @return PendingPerformanceScheduleResponse
     */
    public static PendingPerformanceScheduleResponse toScheduleResponse(PerformanceSchedule schedule) {
        return new PendingPerformanceScheduleResponse(
                schedule.getId(),
                schedule.getStartTime(),
                schedule.getEndTime()
        );
    }
}
