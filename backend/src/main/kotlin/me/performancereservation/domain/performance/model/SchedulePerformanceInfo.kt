package me.performancereservation.domain.performance.model

import java.time.LocalDateTime

/**
 * Dto 매핑을 위해 공연 + 공연회차에서 필요한 값들을 저장하는 데이터 객체
 */

data class SchedulePerformanceInfo( // Performance
    var performanceId: Long,  // 공연 ID
    var title: String,  // 공연 타이틀
    var venue: String,  // 공연 장소
    var price: Int,  // 공연 티켓 가격

    // PerformanceSchedule
    var scheduleId: Long,  // 공연 회차 ID
    var startTime: LocalDateTime,  // 공연 특정 회차의 시작 시간
    var endTime: LocalDateTime // 공연 특정 회차의 끝나는 시간
)
