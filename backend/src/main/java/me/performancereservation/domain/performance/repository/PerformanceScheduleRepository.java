package me.performancereservation.domain.performance.repository;

import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceScheduleRepository extends JpaRepository<PerformanceSchedule, Long> {
    // 공연 아이디로 모든 회차 가져오기
    List<PerformanceSchedule> findByPerformanceId(Long id);
}