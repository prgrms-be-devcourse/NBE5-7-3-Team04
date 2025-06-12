package me.performancereservation.api

import lombok.RequiredArgsConstructor
import me.performancereservation.api.docs.AdminReservationApiDocs
import me.performancereservation.domain.admin.dto.AdminReservationPageResponse
import me.performancereservation.domain.admin.service.AdminReservationService
import me.performancereservation.domain.reservation.enums.ReservationStatus
import me.performancereservation.domain.reservation.service.redis.RedisReservationBulkCancelService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/admin/reservations")
@RequiredArgsConstructor
class AdminReservationController (
    private val adminReservationService: AdminReservationService,
    private val bulkCancelService: RedisReservationBulkCancelService
) : AdminReservationApiDocs {

    /** 예약 목록 조회 매핑
     *
     * @param pageable
     * @return 200 + 페이징 목록
     */
    @GetMapping
    override fun getReservations(pageable: Pageable): ResponseEntity<Page<AdminReservationPageResponse>> {
        return ResponseEntity.ok(
            adminReservationService.getReservationList(pageable)
        )
    }

    /** 예약 검색 목록 매핑
     *
     * @param pageable 페이징
     * @param userName 검색할 회원 이름
     * @param performanceTitle 검색할 공연 제목
     * @param reservationStatus 검색할 예약 상태
     * @param startDate 검색 필터링에 사용할 시작 날짜
     * @param endDate 검색 필터링에 사용할 종료 날짜
     * @return 200 + 검색 페이징 목록
     */
    @GetMapping("/search")
    override fun searchReservations(
        pageable: Pageable,
        @RequestParam(required = false) userName: String,
        @RequestParam(required = false) performanceTitle: String,
        @RequestParam(required = false) reservationStatus: ReservationStatus,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<Page<AdminReservationPageResponse>> {
        return ResponseEntity.ok(
            adminReservationService.searchReservationList(
                pageable,
                userName,
                performanceTitle,
                reservationStatus,
                startDate,
                endDate
            )
        )
    }

    /** 예약 상태 변경
     * 예약 대기 상태라면 확정으로
     *
     * @param reservationId
     */
    @PatchMapping("/{reservationId}")
    override fun confirmReservation(@PathVariable reservationId: Long): ResponseEntity<Void> {
        adminReservationService.confirmReservation(reservationId)
        return ResponseEntity.noContent().build()
    }

    /**
     * 공연 ID 기준으로 예약 일괄 취소를 수동으로 수행
     *
     * @param performanceId 공연 id
     */
    @PostMapping("/cancel/{performanceId}")
    override fun bulkCancelByPerformanceId(@PathVariable performanceId: Long): ResponseEntity<Void> {
        bulkCancelService.cancelAllByPerformanceId(performanceId)
        return ResponseEntity.noContent().build()
    }
}
