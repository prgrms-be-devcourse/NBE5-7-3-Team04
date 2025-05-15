package me.performancereservation.domain.admin.dto;

import me.performancereservation.domain.reservation.enums.ReservationStatus;

import java.time.LocalDateTime;

/** 관리자 예약 목록 응답용
 *
 * @param reservationId 예약 id
 * @param performanceId 공연 id
 * @param performanceScheduleId 회차 id
 * @param name 고객 이름
 * @param title 공연 제목
 * @param price 가격
 * @param quantity 수량
 * @param status 예약 상태
 * @param createdAt 예약 생성 일시
 */
public record AdminReservationPageResponse(
        long reservationId,
        long performanceId,
        long performanceScheduleId,
        String name,
        String title,
        int price,
        int quantity,
        int totalPrice,
        ReservationStatus status,
        LocalDateTime createdAt
) {
}
