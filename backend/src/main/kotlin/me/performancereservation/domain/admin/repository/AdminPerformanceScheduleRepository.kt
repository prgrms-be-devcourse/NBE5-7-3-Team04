package me.performancereservation.domain.admin.repository

import me.performancereservation.domain.performance.entities.PerformanceSchedule
import org.springframework.data.jpa.repository.JpaRepository

interface AdminPerformanceScheduleRepository : JpaRepository<PerformanceSchedule, Long> {
    /**
     * 공연 ID 목록에 해당하는 모든 공연 스케줄을 조회
     *
     * @param performanceIds 공연 ID 목록
     * @return 조회된 공연 스케줄 목록
     */
    fun findByPerformanceIdIn(performanceIds: List<Long>): List<PerformanceSchedule>
}