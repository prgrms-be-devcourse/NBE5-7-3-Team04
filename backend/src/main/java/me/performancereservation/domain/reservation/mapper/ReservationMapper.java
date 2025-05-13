package me.performancereservation.domain.reservation.mapper;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
    @Value("${reservation.expired_time}")
    private int EXPIRE_MINUTES;

    /**
     * 예약 객체와 공연+공연회차 데이터 모델 객체를 이용해 예약 응답 Dto 생성
     *
     * @param reservation 예약 객체
     * @param schedulePerformanceInfo 공연+공연회차 데이터 모델 객체
     * @return ReservationResponse 예약 응답 dto
     */
    public ReservationResponse toResponseDto(Reservation reservation, SchedulePerformanceInfo schedulePerformanceInfo) {
        LocalDateTime createdAt = reservation.getCreatedAt();
        LocalDateTime expirationAt = createdAt.plusMinutes(EXPIRE_MINUTES);

        int totalPrice = reservation.calculateTotalPrice(schedulePerformanceInfo.price());

        return new ReservationResponse(
                reservation.getId(),
                schedulePerformanceInfo.title(),
                schedulePerformanceInfo.venue(),
                reservation.getQuantity(),
                reservation.getStatus(),
                createdAt,
                expirationAt,
                schedulePerformanceInfo.price(),
                totalPrice
        );
    }
}