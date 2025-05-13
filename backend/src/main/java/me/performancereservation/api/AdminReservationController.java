package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.admin.dto.AdminReservationPageResponse;
import me.performancereservation.domain.admin.service.AdminReservationService;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    @GetMapping
    public ResponseEntity<Page<AdminReservationPageResponse>> getReservations(Pageable pageable) {
        return ResponseEntity.ok(adminReservationService.getReservationList(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AdminReservationPageResponse>> searchReservations(Pageable pageable,
                                                                                 @RequestParam(required = false) String userName,
                                                                                 @RequestParam(required = false) String performanceTitle,
                                                                                 @RequestParam(required = false)ReservationStatus reservationStatus,
                                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(adminReservationService.searchReservationList(
                pageable,
                userName,
                performanceTitle,
                reservationStatus,
                startDate,
                endDate
        ));
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<Void> confirmReservation(@PathVariable Long reservationId) {
        adminReservationService.confirmReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

}
