package me.performancereservation.domain.performance.event


/**
 * 회차 생성 이벤트
 */
data class PerformanceScheduleCreatedEvent(val scheduleId: Long, val totalSeats: Int)

/**
 * 특정 공연 회차가 취소됐을 때, 공연 회차 취소 이벤트리스너에게 전송할 이벤트 파라미터
 *
 * @param scheduleId 취소된 공연 회차의 id
 */
data class ScheduleCanceledEvent(val scheduleId: Long)
