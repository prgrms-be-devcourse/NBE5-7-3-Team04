package me.performancereservation.api

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import me.performancereservation.api.docs.ReservationApiDocs
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse
import me.performancereservation.domain.reservation.dto.ReservationPageResponse
import me.performancereservation.domain.reservation.dto.ReservationRequest
import me.performancereservation.domain.reservation.dto.ReservationResponse
import me.performancereservation.domain.reservation.service.ReservationQueryService
import me.performancereservation.domain.reservation.service.redis.RedisSeatReservationService
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
    private val seatReservationService: RedisSeatReservationService,
    private val reservationQueryService: ReservationQueryService
) : ReservationApiDocs {


    @PostMapping
    override fun reserve(
        @RequestBody request: @Valid ReservationRequest,
        @AuthenticationPrincipal authentication: CustomOAuth2User
    ): ResponseEntity<ReservationResponse> {
        val result = seatReservationService.reserve(
            request.performanceId,
            request.scheduleId,
            authentication.user.id!!,
            request.quantity
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @PostMapping("/{reservationId}/cancel")
    override fun cancel(
        @PathVariable reservationId: Long,
        @AuthenticationPrincipal authentication: CustomOAuth2User
    ): ResponseEntity<Map<String, Long>> {
        log.info { "예약 취소 호출" }

        val refundId = seatReservationService.cancel(
            reservationId,
            authentication.user.id!!
        )

        log.info { "예약 취소 성공, refundId = $refundId" }

        return ResponseEntity.ok(mapOf("refundId" to refundId))
    }

    @GetMapping("/me")
    override fun getUserReservations(
        @AuthenticationPrincipal authentication: CustomOAuth2User,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<ReservationPageResponse>> {
        val result = reservationQueryService.getAllByUserId(
            authentication.user.id!!,
            pageable
        )

        return ResponseEntity.ok(result)
    }

    @GetMapping("/me/{reservationId}")
    override fun getReservationById(
        @AuthenticationPrincipal authentication: CustomOAuth2User,
        @PathVariable reservationId: Long
    ): ResponseEntity<ReservationDetailResponse> {
        val result = reservationQueryService.getByReservationId(
            reservationId,
            authentication.user.id!!
        )

        return ResponseEntity.ok(result)
    }
}