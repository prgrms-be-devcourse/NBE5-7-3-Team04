package me.performancereservation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.api.docs.ReservationApiDocs;
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse;
import me.performancereservation.domain.reservation.dto.ReservationPageResponse;
import me.performancereservation.domain.reservation.service.ReservationQueryService;
import me.performancereservation.domain.reservation.service.redis.RedisReservationBulkCancelService;
import me.performancereservation.domain.reservation.service.redis.RedisSeatReservationService;
import me.performancereservation.domain.reservation.dto.ReservationRequest;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController implements ReservationApiDocs {
    private final RedisSeatReservationService seatReservationService;
    private final ReservationQueryService reservationQueryService;

    @Override
    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @RequestBody @Valid ReservationRequest request,
            @AuthenticationPrincipal CustomOAuth2User authentication
    ) {
        ReservationResponse result = seatReservationService.reserve(
                request.getPerformanceId(),
                request.getScheduleId(),
                authentication.user.getId(),
                request.getQuantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Map<String, Long>> cancel(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomOAuth2User authentication
    ) {
        log.info("예약 취소 호출");
        Long refundId = seatReservationService.cancel(
                reservationId,
                authentication.user.getId()
        );
        log.info("예약 취소 성공");

        Map<String, Long> response = new HashMap<>();
        response.put("refundId", refundId);
        log.info("refundId: {}", refundId);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<Page<ReservationPageResponse>> getUserReservations(
            @AuthenticationPrincipal CustomOAuth2User authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReservationPageResponse> result = reservationQueryService.getAllByUserId(
                authentication.user.getId(),
                pageable
        );

        return ResponseEntity.ok(result);
    }

    @Override
    @GetMapping("/me/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> getReservationById(
            @AuthenticationPrincipal CustomOAuth2User authentication,
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(reservationQueryService.getByReservationId(
                reservationId,
                authentication.user.getId()
        ));
    }
}
