package me.performancereservation.global.init;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.global.storage.redis.RedisSeatService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MockDataLoader implements CommandLineRunner {
    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final RedisSeatService redisSeatService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(String... args) {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 공연 mock 생성 (좌석 수 + 공연 등록자 ID)
        List<Performance> performances = List.of(
                Performance.builder()
                        .category(PerformanceCategory.SINGING)
                        .title("옥탑방고양이1")
                        .description("야옹1")
                        .price(8000)
                        .venue("서울 어딘가")
                        .managerId(1L)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .totalSeats(100)
                        .status(PerformanceStatus.CONFIRMED)
                        .build(),
                Performance.builder()
                        .category(PerformanceCategory.SINGING)
                        .title("옥탑방고양이2")
                        .description("야옹2")
                        .price(10000)
                        .venue("경기도 어딘가")
                        .managerId(2L)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .totalSeats(10)
                        .status(PerformanceStatus.CONFIRMED)
                        .build(),
                Performance.builder()
                        .category(PerformanceCategory.SINGING)
                        .title("옥탑방고양이3")
                        .description("야옹3")
                        .price(10000)
                        .venue("부산 어딘가")
                        .managerId(3L)
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .totalSeats(3)
                        .status(PerformanceStatus.CONFIRMED)
                        .build()
        );

        performanceRepository.saveAll(performances);

        List<PerformanceSchedule> allSchedules = new ArrayList<>();

        for (Performance performance : performances) {
            for (int i = 0; i < 3; i++) { // 회차 3개씩 생성
                PerformanceSchedule schedule = PerformanceSchedule.builder()
                        .performanceId(performance.getId())
                        .startTime(LocalDateTime.now().plusDays(i))
                        .endTime(LocalDateTime.now().plusDays(i).plusHours(2))
                        .remainingSeats(performance.getTotalSeats())
                        .canceled(false)
                        .build();

                allSchedules.add(schedule);
            }
        }

        performanceScheduleRepository.saveAll(allSchedules);

        // Redis 좌석 초기화
        for (PerformanceSchedule schedule : allSchedules) {
            redisSeatService.initializeSeatStock(schedule.getId(), schedule.getRemainingSeats());
        }
    }

}
