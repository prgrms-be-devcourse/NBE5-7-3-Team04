package me.performancereservation.global.storage.redis;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.event.PerformanceScheduleCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSeatInitializationListener {
    private final RedisSeatService redisSeatService;

    @EventListener
    public void handlePerformanceScheduleCreated(PerformanceScheduleCreatedEvent event) {
        redisSeatService.initializeSeatStock(event.scheduleId(), event.totalSeats());
    }
}
