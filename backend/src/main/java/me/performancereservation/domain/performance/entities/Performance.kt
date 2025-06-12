package me.performancereservation.domain.performance.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.performance.dto.performance.request.PerformanceUpdateRequest;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.global.exception.ErrorCode;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Performance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 공연 ID

    private Long fileId; // (FK) 파일 ID - 공연 썸네일 용도

    private Long managerId; // (FK) 공연관리자 ID

    private String title; // 제목

    private String venue; // 공연 장소

    private int price; // 가격

    private int totalSeats; // 총 좌석수

    @Enumerated(EnumType.STRING)
    private PerformanceCategory category; // 공연 분류

    private LocalDateTime startDate; // 공연 시작 일시

    private LocalDateTime endDate; // 공연 종료 일시

    private String description; // 설명

    @Enumerated(EnumType.STRING)
    private PerformanceStatus status; // 공연 상태

    @Builder
    public Performance(Long id, Long fileId, Long managerId, String title, String venue, int price, int totalSeats, PerformanceCategory category, LocalDateTime startDate, LocalDateTime endDate, String description, PerformanceStatus status) {
        this.id = id;
        this.fileId = fileId;
        this.managerId = managerId;
        this.title = title;
        this.venue = venue;
        this.price = price;
        this.totalSeats = totalSeats;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.status = status;
    }


    public boolean isPending() {
        return this.status == PerformanceStatus.PENDING;
    }

    public void confirm() {
        this.status = PerformanceStatus.CONFIRMED;
    }

    public void reject() {
        this.status = PerformanceStatus.REJECTED;
    }

    public void updateFrom(PerformanceUpdateRequest request) {
        this.fileId = request.fileId();
        this.description = request.description();
    }

    public void cancel() {
        this.status = PerformanceStatus.CANCELLED;
    }

    public boolean isRegistrationPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return (!startDate.isBefore(this.startDate) && !endDate.isAfter(this.endDate)) && (startDate.isBefore(endDate));
    }

    public boolean hasFile() {
        return this.fileId != null;
    }

    public boolean hasPermission(Long managerId) {
        return this.managerId != null && this.managerId.equals(managerId);
    }

    public boolean isConfirmed() {
        return this.status == PerformanceStatus.CONFIRMED;

    }

    public void completePerformance() {
        this.status = PerformanceStatus.COMPLETED;
    }
}
