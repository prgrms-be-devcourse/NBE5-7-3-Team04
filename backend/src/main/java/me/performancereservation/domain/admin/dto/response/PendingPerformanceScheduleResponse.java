package me.performancereservation.domain.admin.dto.response;

import java.time.LocalDateTime;

/** 어드민 공연 승인 회차 정보 확인용 응답
 *
 * @param id            // 공연 회차 ID
 * @param startTime     // 공연(회차) 시작 시간
 * @param endTime       // 공연(회차) 종료 시간
 */
public record PendingPerformanceScheduleResponse(
        long id,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
