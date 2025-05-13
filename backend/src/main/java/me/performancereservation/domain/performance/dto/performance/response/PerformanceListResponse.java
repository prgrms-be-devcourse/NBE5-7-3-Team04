package me.performancereservation.domain.performance.dto.performance.response;

import java.time.LocalDateTime;

/** 고객 공연 목록 페이지 응답용
 *
 * @param id
 * @param fileUrl   // 썸네일 파일 주소
 * @param title
 * @param price
 * @param performanceDate
 * @param venue
 */
public record PerformanceListResponse(
        Long id,
        String fileUrl,
        String title,
        int price,
        LocalDateTime performanceDate,
        String venue
) {
}
