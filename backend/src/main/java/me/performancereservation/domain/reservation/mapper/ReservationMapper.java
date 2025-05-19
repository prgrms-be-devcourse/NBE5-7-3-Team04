package me.performancereservation.domain.reservation.mapper;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse;
import me.performancereservation.domain.reservation.dto.ReservationPageResponse;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.domain.ticket.Ticket;
import me.performancereservation.domain.ticket.TicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationMapper {
    private final TicketRepository ticketRepository;

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

        // 관련 티켓들 조회
        List<Ticket> tickets = ticketRepository.findAllByReservationId(reservation.getId());

        // 티켓 id 목록 생성
        List<String> ticketNumbers = tickets.stream()
                .map(ticket -> reservation.getId() + "-" + ticket.getId().toString())
                .toList();

        return new ReservationResponse(
                reservation.getId(),
                schedulePerformanceInfo.title(),
                schedulePerformanceInfo.venue(),
                reservation.getQuantity(),
                reservation.getStatus(),
                createdAt,
                expirationAt,
                schedulePerformanceInfo.price(),
                totalPrice,
                ticketNumbers
        );
    }

    /**
     * 예약 객체와 공연+공연회차 데이터 모델 객체를 이용해 예약 응답 Dto 생성
     *
     * @param reservation 예약 객체
     * @param schedulePerformanceInfo 공연+공연회차 데이터 모델 객체
     * @return ReservationResponse 예약 응답 dto
     */
    public ReservationDetailResponse toDetailResponseDto(Reservation reservation, SchedulePerformanceInfo schedulePerformanceInfo, Performance performance, String fileUrl) {
        LocalDateTime createdAt = reservation.getCreatedAt();
        LocalDateTime expirationAt = createdAt.plusMinutes(EXPIRE_MINUTES);

        int totalPrice = reservation.calculateTotalPrice(schedulePerformanceInfo.price());

        // 관련 티켓들 조회
        List<Ticket> tickets = ticketRepository.findAllByReservationId(reservation.getId());

        // 티켓 id 목록 생성
        List<String> ticketNumbers = tickets.stream()
                .map(ticket -> reservation.getId() + "-" + ticket.getId().toString())
                .toList();

        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getPerformanceId(),
                schedulePerformanceInfo.title(),
                performance.getDescription(),
                schedulePerformanceInfo.venue(),
                fileUrl,
                reservation.getQuantity(),
                reservation.getStatus(),
                createdAt,
                expirationAt,
                schedulePerformanceInfo.startTime(),
                schedulePerformanceInfo.endTime(),
                schedulePerformanceInfo.price(),
                totalPrice,
                ticketNumbers
        );
    }

    /**
     * 예약 객체와 공연+공연회차 데이터 모델 객체를 이용해 예약 응답 Dto Page 생성
     *
     * @param reservation 예약 객체
     * @param schedulePerformanceInfo 공연+공연회차 데이터 모델 객체
     * @return ReservationListResponse 예약 목록 응답 dto
     */
    public ReservationPageResponse toListResponseDto(Reservation reservation, SchedulePerformanceInfo schedulePerformanceInfo) {
        LocalDateTime createdAt = reservation.getCreatedAt();

        int totalPrice = reservation.calculateTotalPrice(schedulePerformanceInfo.price());

        return new ReservationPageResponse(
                reservation.getId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                createdAt,
                schedulePerformanceInfo.title(),
                schedulePerformanceInfo.venue(),
                schedulePerformanceInfo.price(),
                totalPrice
        );
    }
}