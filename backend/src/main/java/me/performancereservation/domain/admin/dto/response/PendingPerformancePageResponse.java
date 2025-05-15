package me.performancereservation.domain.admin.dto.response;

import me.performancereservation.domain.performance.enums.PerformanceCategory;

import java.time.LocalDateTime;
import java.util.List;

/** 어드민 공연 승인 확인용 공연 정보 응답
 *
 * @param id                        // 공연 ID
 * @param fileUrl                   // 공연 이미지 URL
 * @param performanceManagerName    // 공연 관리자 이름
 * @param title                     // 공연 제목
 * @param venue                     // 공연 장소
 * @param price                     // 공연 가격
 * @param totalSeats                // 공연 전체 좌석 수
 * @param category                  // 공연 카테고리
 * @param startDate                 // 공연 시작일
 * @param endDate                   // 공연 종료일
 * @param description               // 공연 설명
 * @param schedules                 // 공연 회차 정보 리스트
 */
public record PendingPerformancePageResponse(
        long id,
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
