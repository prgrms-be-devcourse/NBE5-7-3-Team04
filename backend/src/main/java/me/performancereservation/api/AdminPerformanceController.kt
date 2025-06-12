package me.performancereservation.api

import lombok.RequiredArgsConstructor
import me.performancereservation.api.docs.AdminPerformanceApiDocs
import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse
import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse
import me.performancereservation.domain.admin.service.AdminPerformanceService
import me.performancereservation.domain.performance.enums.PerformanceStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminPerformanceController(
    private val adminPerformanceService: AdminPerformanceService
) : AdminPerformanceApiDocs {

    /** 공연을 상태별로 페이징으로 조회
     *
     * @param pageable
     * @return Page<PendingPerformancePageResponse>
    </PendingPerformancePageResponse> */
    @GetMapping("/performances")
    override fun performances(
        @RequestParam(required = false) status: PerformanceStatus?,
        pageable: Pageable
    ): ResponseEntity<Page<PendingPerformancePageResponse>> {
        val pendingPerformanceList: Page<PendingPerformancePageResponse> =
            adminPerformanceService.getPendingPerformanceList(pageable, status)
        return ResponseEntity.ok(pendingPerformanceList)
    }

    /** PENDING 상태의 공연을 승인
     *
     * @param performanceId
     */
    @PostMapping("/performances/{performanceId}/confirm")
    override fun confirmPerformance(@PathVariable("performanceId") performanceId: Long): ResponseEntity<Void?> {
        adminPerformanceService.confirmPerformance(performanceId)
        return ResponseEntity.noContent().build<Void?>()
    }

    /** PENDING 상태의 공연을 거부
     *
     * @param performanceId
     */
    @PostMapping("/performances/{performanceId}/reject")
    override fun rejectPerformance(@PathVariable("performanceId") performanceId: Long): ResponseEntity<Void> {
        adminPerformanceService.rejectPerformance(performanceId)
        return ResponseEntity.noContent().build()
    }

    /** PENDING 상태의 공연 관리자 요청을 페이징으로 조회
     *
     * @param pageable
     * @return Page<PendingManagerRequestPageResponse>
    </PendingManagerRequestPageResponse> */
    @GetMapping("/pending-manager-requests")
    override fun pendingManagerRequests(pageable: Pageable): ResponseEntity<Page<PendingManagerRequestPageResponse>> {
        val pendingManagerRequestList: Page<PendingManagerRequestPageResponse> =
            adminPerformanceService.getPendingManagerRequestList(pageable)

        return ResponseEntity.ok(pendingManagerRequestList)
    }

    /** PENDING 상태의 공연 관리자 요청을 승인
     *
     * @param managerRequestId
     */
    @PostMapping("/manager-requests/{managerRequestId}/approve")
    override fun approveManagerRequest(@PathVariable("managerRequestId") managerRequestId: Long): ResponseEntity<Void> {
        adminPerformanceService.approveManagerRequest(managerRequestId)
        return ResponseEntity.noContent().build()
    }

    /** PENDING 상태의 공연 관리자 요청을 거부
     *
     * @param managerRequestId
     */
    @PostMapping("/manager-requests/{managerRequestId}/reject")
    override fun rejectManagerRequest(@PathVariable("managerRequestId") managerRequestId: Long): ResponseEntity<Void> {
        adminPerformanceService.rejectManagerRequest(managerRequestId)
        return ResponseEntity.noContent().build()
    }
}
