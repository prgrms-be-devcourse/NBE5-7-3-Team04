package me.performancereservation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final RedisSeatReservationService seatReservationService;
    private final RedisReservationBulkCancelService bulkCancelService;
    private final ReservationQueryService reservationQueryService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @RequestBody @Valid ReservationRequest request,
            @AuthenticationPrincipal CustomOAuth2User authentication
    ) {

        ReservationResponse result = seatReservationService.reserve(
                request.scheduleId(),
                authentication.getUser().getId(),
                request.quantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long reservationId,
             @AuthenticationPrincipal CustomOAuth2User authentication
    ) {
        seatReservationService.cancel(
                reservationId,
                authentication.getUser().getId()
        ) ;

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ReservationPageResponse>> getUserReservations(
            @AuthenticationPrincipal CustomOAuth2User authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReservationPageResponse> result = reservationQueryService.getAllByUserId(
                authentication.getUser().getId(),
                pageable
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/me/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @AuthenticationPrincipal CustomOAuth2User authentication,
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(reservationQueryService.getByReservationId(
                reservationId,
                authentication.getUser().getId()
        ));
    }
}
