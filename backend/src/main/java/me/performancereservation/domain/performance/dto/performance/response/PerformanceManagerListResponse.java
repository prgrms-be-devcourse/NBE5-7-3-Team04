package me.performancereservation.domain.performance.dto.performance.response;

import java.time.LocalDateTime;

/** 공연 관리자 공연 목록 페이지 응답용
 *
 * @param id
 * @param fileUrl
 * @param title
 * @param performanceDate
 * @param venue
 * @param status    // 공연 등록 여부 (PENDING, CONFIRM 등)
 */
public record PerformanceManagerListResponse(
        Long id,
        String fileUrl,
        String title,
        LocalDateTime performanceDate,
        String venue,
        String status
) {
}
