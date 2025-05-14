package me.performancereservation.domain.performance.dto.performance.response;

import me.performancereservation.domain.performance.enums.PerformanceCategory;

import java.time.LocalDateTime;

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
public record PerformancePageResponse(
        Long id,
        String fileUrl,
        String title,
        int price,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String venue,
        PerformanceCategory category
) {
}
