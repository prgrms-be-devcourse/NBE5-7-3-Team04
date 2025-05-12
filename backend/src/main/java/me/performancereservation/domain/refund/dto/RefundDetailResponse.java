package me.performancereservation.domain.refund.dto;

import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.refund.Refund;
import me.performancereservation.domain.refund.enums.RefundStatus;

import java.time.LocalDateTime;

public record RefundDetailResponse(
        // Refund에서 데이터 전달
        Long refundId,
        Long userId,
        Long reservationId,
        String account,
        String bank,
        String depositOwner,
        RefundStatus refundStatus,

        // Reservation에서 가져오는 데이터
        Integer quantity,

        // PerformanceSchedule에서 가져오는 데이터
        LocalDateTime startTime,

        // Performance에서 가져오는 데이터 (id, totalSeats 외 모두)
        Long fileId, // (FK) 파일 ID - 공연 썸네일 용도
        String title, // 제목
        String venue, // 공연 장소
        Integer price, // 가격
        String category, // 공연 분류
        LocalDateTime performance_date, // 공연 일시
        String description // 설명
) {
    public static RefundDetailResponse fromEntity(Refund refund, Integer reservationQuantity, LocalDateTime startTime, Performance performance) {
        return new RefundDetailResponse(
                refund.getId(),
                refund.getUserId(),
                refund.getReservationId(),
                refund.getAccount(),
                refund.getBank(),
                refund.getDepositorName(),
                refund.getStatus(),

                reservationQuantity,
                startTime,

                performance.getFileId(),
                performance.getTitle(),
                performance.getVenue(),
                performance.getPrice(),
                performance.getCategory().toString(),
                performance.getPerformance_date(),
                performance.getDescription()
        );
    }
}
