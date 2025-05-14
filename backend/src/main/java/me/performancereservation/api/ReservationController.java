package me.performancereservation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.reservation.dto.ReservationPageResponse;
import me.performancereservation.domain.reservation.service.ReservationQueryService;
import me.performancereservation.domain.reservation.service.redis.RedisReservationBulkCancelService;
import me.performancereservation.domain.reservation.service.redis.RedisSeatReservationService;
import me.performancereservation.domain.reservation.dto.ReservationRequest;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestBody @Valid ReservationRequest request
//            ,@AuthenticationPrincipal JwtAuthentication authentication TODO Authentication 구현 끝나면 주석 해제할 예정
    ) {
//       TODO Authentication 구현 끝나면 주석 해제할 예정
//        ReservationResponse result = reservationService.reserve(
//                request.scheduleId(),
//                authentication.userId(),
//                request.quantity()
//        );

        ReservationResponse result = seatReservationService.reserve(
                request.scheduleId(),
                request.userId(),
                request.quantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long reservationId
            // @AuthenticationPrincipal Authentication authentication // TODO Authentication 머지 되면 수정 예정
    ) {
        seatReservationService.cancel(
                reservationId,
                1L // TODO 수정 예정
                // authentication.userId() // TODO Authentication 머지 되면 수정 예정
        ) ;

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ReservationPageResponse>> getUserReservations(
//            @AuthenticationPrincipal JwtAuthentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReservationPageResponse> result = reservationQueryService.getAllByUserId(
//                authentication.userId(), // TODO Authentication 머지 되면 수정 예정
                1L, // TODO 수정 예정
                pageable
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/me/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationById(
//            @AuthenticationPrincipal JwtAuthentication authentication,
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(reservationQueryService.getByReservationId(
                reservationId,
//                authentication.userId() // TODO Authentication 머지 되면 수정 예정
                1L // TODO 수정 예정
        ));
    }

    // 공연 ID 기준으로 예약 일괄 취소를 수동으로 수행
    @PostMapping("/cancel/{performanceId}")
    // TODO 어드민 권한 부착하거나 어드민 컨트롤러 구현되면 그쪽으로 이동
    public ResponseEntity<Void> bulkCancelByPerformanceId(@PathVariable Long performanceId) {
        bulkCancelService.cancelAllByPerformanceId(performanceId);

        return ResponseEntity.noContent().build();
    }
}
