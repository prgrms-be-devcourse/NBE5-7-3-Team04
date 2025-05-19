package me.performancereservation.domain.reservation.dto;

import me.performancereservation.domain.reservation.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 정보 응답 Dto
 */
public record ReservationDetailResponse(
        long reservationId, // 공연 예약 ID
        long performanceId, // 공연 ID
        String title, // 공연 제목
        String description, // 공연 설명
        String venue, // 공연 위치
        String fileUrl, // 이미지 경로
        int quantity, // 티켓 수량
        ReservationStatus status, // 예약 상태
        LocalDateTime createdAt, // 예약 신청 일시
        LocalDateTime expirationAt, // 결제 만료 일시
        LocalDateTime startTime, // 회차 시작 시간
        LocalDateTime endTime, // 회차 종료 시간
        int ticketPrice, // 티켓 가격
        int totalPrice, // 총 결제 금액
        List<String> ticketNumbers // 티켓 번호 목록
) {}