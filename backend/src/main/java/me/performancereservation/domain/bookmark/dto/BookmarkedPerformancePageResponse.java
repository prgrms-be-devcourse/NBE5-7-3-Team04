package me.performancereservation.domain.bookmark.dto;

import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;

import java.time.LocalDateTime;

/** 북마크 된 공연 목록 페이징 응답
 *
 * @param id
 * @param fileUrl
 * @param title
 * @param price
 * @param performanceDate
 * @param venue
 * @param description
 * @param category
 * @param status
 * @param bookmarked //북마크 되어있는지 여부
 */
public record BookmarkedPerformancePageResponse(
        Long id,
        String fileUrl,
        String title,
        int price,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String venue,
        String description,
        PerformanceCategory category,
        PerformanceStatus status,
        boolean bookmarked
) {
}
