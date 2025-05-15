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
 * @param startDate
 * @param endDate
 * @param venue
 * @param category
 * @param status
 * @param bookmarked //북마크 되어있는지 여부
 */
public record BookmarkedPerformancePageResponse(
        long id,                            // 공연 ID
        String fileUrl,                     // 공연 이미지 URL
        String title,                       // 공연 제목
        int price,                          // 공연 가격
        LocalDateTime startDate,            // 공연 시작일
        LocalDateTime endDate,              // 공연 종료일
        String venue,                       // 공연 장소
        PerformanceCategory category,       // 공연 카테고리
        PerformanceStatus status,           // 공연 상태
        boolean bookmarked                  // 공연 북마크 여부
) {
}
