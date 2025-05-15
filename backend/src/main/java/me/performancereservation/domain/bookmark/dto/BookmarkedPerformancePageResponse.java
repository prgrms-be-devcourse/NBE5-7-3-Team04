package me.performancereservation.domain.bookmark.dto;

import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;

import java.time.LocalDateTime;

/** 북마크 된 공연 목록 페이징 응답
 *
 * @param id             // 공연 ID
 * @param fileUrl        // 공연 이미지 URL
 * @param title          // 공연 제목
 * @param price          // 공연 가격
 * @param startDate      // 공연 시작일
 * @param endDate        // 공연 종료일
 * @param venue          // 공연 장소
 * @param category       // 공연 카테고리
 * @param status         // 공연 상태
 * @param bookmarked     // 공연 북마크 여부
 */
public record BookmarkedPerformancePageResponse(
        long id,
        String fileUrl,
        String title,
        int price,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String venue,
        PerformanceCategory category,
        PerformanceStatus status,
        boolean bookmarked
) {
}
