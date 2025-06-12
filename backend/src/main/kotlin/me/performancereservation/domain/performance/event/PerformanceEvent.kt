package me.performancereservation.domain.performance.event

/**
 * 공연이 취소됐을 때, 공연 취소 이벤트리스너에게 전송할 이벤트 파라미터
 *
 * @param performanceId 취소된 공연의 id
 */
data class PerformanceCanceledEvent(val performanceId: Long)