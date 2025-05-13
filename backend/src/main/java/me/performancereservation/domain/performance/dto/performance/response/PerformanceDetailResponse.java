package me.performancereservation.domain.performance.dto.performance.response;

import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleResponse;

import java.util.List;

/** 상세 페이지 응답용
 *
 * @param id
 * @param title
 * @param price
 * @param totalSeats
 * @param venue
 * @param description
 * @param status        // 판매중 여부 (공연 예약이 가능한 상태인지)
 * @param fileUrl
 * @param schedules     // 회차 정보
 */
public record PerformanceDetailResponse(
        Long id,
        String title,
        int price,
        int totalSeats,
        String venue,
        String description,
        String status,
        String fileUrl,
        List<PerformanceScheduleResponse> schedules
        ) {}
