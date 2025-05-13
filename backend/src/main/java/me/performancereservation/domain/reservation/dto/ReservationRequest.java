package me.performancereservation.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 유저의 예약 시도를 위한 요청 Dto
 */
public record ReservationRequest(
        @NotNull Long scheduleId, // 공연 회차 ID
        @NotNull Long userId, // 유저 ID TODO Authentication 구현 끝나면 제거
        @NotNull @Positive int quantity // 티켓수량
) {}