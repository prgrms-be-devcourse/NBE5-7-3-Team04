package me.performancereservation.domain.admin.dto.response;

import me.performancereservation.domain.performance.enums.PerformanceCategory;

import java.time.LocalDateTime;
import java.util.List;

public record PendingPerformancePageResponse(
        Long id,
        String fileUrl,
        String performanceManagerName,
        String title,
        String venue,
        int price,
        int totalSeats,
        PerformanceCategory category,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String description,
        List<PendingPerformanceScheduleResponse> schedules
) {

}
