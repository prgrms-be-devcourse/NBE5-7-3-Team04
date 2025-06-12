package me.performancereservation.domain.performance.repository;

import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface PerformanceScheduleRepository extends JpaRepository<PerformanceSchedule, Long> {
    // scheduleId로 DTO 프로젝션을 이용해서 SchedulePerformanceInfo 데이터 모델을 조회
    @Query("""
        SELECT new me.performancereservation.domain.performance.model.SchedulePerformanceInfo(
            p.id, p.title, p.venue, p.price,
            ps.id, ps.startTime, ps.endTime
        )
        FROM PerformanceSchedule ps
        JOIN Performance p ON ps.performanceId = p.id
        WHERE ps.id = :scheduleId
    """)
    Optional<SchedulePerformanceInfo> findSchedulePerformanceInfoByScheduleId(@Param("scheduleId") Long scheduleId);

    // scheduleId로 DTO 프로젝션을 이용해서 in절로 SchedulePerformanceInfo 데이터 모델 리스트를 조회
    @Query("""
        SELECT new me.performancereservation.domain.performance.model.SchedulePerformanceInfo(
            p.id, p.title, p.venue, p.price,
            ps.id, ps.startTime, ps.endTime
        )
        FROM PerformanceSchedule ps
        JOIN Performance p ON ps.performanceId = p.id
        WHERE ps.id IN :scheduleIds
    """)
    List<SchedulePerformanceInfo> findAllSchedulePerformanceInfoByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    // performanceId로 모든 회차 id 조회
    @Query("""
        SELECT ps.id
        FROM PerformanceSchedule ps
        WHERE ps.performanceId = :performanceId
    """)
    List<Long> findIdsByPerformanceId(@Param("performanceId") Long performanceId);

    // 공연 아이디로 모든 회차 가져오기(시작 시간 오름차순 - 시작 시간이 빠른것 부터)
    List<PerformanceSchedule> findByPerformanceIdOrderByStartTimeAsc(Long id);

    boolean existsByIdAndPerformanceId(Long id, Long performanceId);
}