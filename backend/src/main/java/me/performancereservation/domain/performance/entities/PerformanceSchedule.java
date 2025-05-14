package me.performancereservation.domain.performance.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.global.exception.ErrorCode;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class PerformanceSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long performanceId; // (FK) 공연 ID

    private LocalDateTime startTime; // 공연 시작 시간

    private LocalDateTime endTime; // 공연 종료 시간

    private int remainingSeats; // 남은 좌석

    @Column(name = "is_canceled")
    private boolean canceled; // 회차 취소 여부

    @Builder
    public PerformanceSchedule(Long id, Long performanceId, LocalDateTime startTime, LocalDateTime endTime, int remainingSeats, boolean canceled) {
        this.id = id;
        this.performanceId = performanceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.remainingSeats = remainingSeats;
        this.canceled = canceled;
    }

    public void cancel() {
        this.canceled = true;
    }

    public void decreaseRemainingSeats(int quantity) {
        if(this.remainingSeats < quantity) {
            throw ErrorCode.NO_REMAINING_SEATS.domainException("잔여 좌석이 부족합니다. remainingSeats = " + this.remainingSeats);
        }
        this.remainingSeats -= quantity;
    }

    public boolean hasPermission(Long performanceId) {
        return performanceId != null && performanceId.equals(this.performanceId);
    }
}