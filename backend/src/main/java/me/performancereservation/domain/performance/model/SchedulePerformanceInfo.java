package me.performancereservation.domain.performance.model;

import java.time.LocalDateTime;

/**
 * Dto 매핑을 위해 공연 + 공연회차에서 필요한 값들을 저장하는 데이터 객체
 */
public record SchedulePerformanceInfo(
        // Performance
        Long performanceId, // 공연 ID
        String title, // 공연 타이틀
        String venue, // 공연 장소
        int price, // 공연 티켓 가격

        // PerformanceSchedule
        Long scheduleId, // 공연 회차 ID
        LocalDateTime startTime, // 공연 특정 회차의 시작 시간
        LocalDateTime endTime // 공연 특정 회차의 끝나는 시간
) {}
