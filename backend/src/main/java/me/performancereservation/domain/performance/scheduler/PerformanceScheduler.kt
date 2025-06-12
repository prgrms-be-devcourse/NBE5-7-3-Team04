package me.performancereservation.domain.performance.scheduler;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.service.PerformanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformanceScheduler {

    private final PerformanceService performanceService;

    @Scheduled(cron = "0 0 0 * * *")
    public void completeEndedPerformances() {
        performanceService.completeEndedPerformances();
    }
}
