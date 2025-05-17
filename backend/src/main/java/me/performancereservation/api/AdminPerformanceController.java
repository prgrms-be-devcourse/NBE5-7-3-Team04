package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.api.docs.AdminPerformanceApiDocs;
import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse;
import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse;
import me.performancereservation.domain.admin.service.AdminPerformanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminPerformanceController implements AdminPerformanceApiDocs {

    private final AdminPerformanceService adminPerformanceService;

    /** PENDING 상태의 공연을 페이징으로 조회
     *
     * @param pageable
     * @return Page<PendingPerformancePageResponse>
     */
    @Override
    @GetMapping("/performances")
    public ResponseEntity<Page<PendingPerformancePageResponse>> performances(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<PendingPerformancePageResponse> pendingPerformanceList = adminPerformanceService.getPendingPerformanceList(pageable);
        return ResponseEntity.ok(pendingPerformanceList);
    }

    /** PENDING 상태의 공연을 승인
     *
     * @param performanceId
     */
    @Override
    @PostMapping("/performances/{performanceId}/confirm")
    public ResponseEntity<Void> confirmPerformance(@PathVariable("performanceId") Long performanceId) {
        adminPerformanceService.confirmPerformance(performanceId);
        return ResponseEntity.noContent().build();
    }

    /** PENDING 상태의 공연을 거부
     *
     * @param performanceId
     */
    @Override
    @PostMapping("/performances/{performanceId}/reject")
    public ResponseEntity<Void> rejectPerformance(@PathVariable("performanceId") Long performanceId) {
        adminPerformanceService.rejectPerformance(performanceId);
        return ResponseEntity.noContent().build();
    }

    /** PENDING 상태의 공연 관리자 요청을 페이징으로 조회
     *
     * @param pageable
     * @return Page<PendingManagerRequestPageResponse>
     */
    @Override
    @GetMapping("/pending-manager-requests")
    public ResponseEntity<Page<PendingManagerRequestPageResponse>> pendingManagerRequests(Pageable pageable) {
        Page<PendingManagerRequestPageResponse> pendingManagerRequestList = adminPerformanceService.getPendingManagerRequestList(pageable);
        return ResponseEntity.ok(pendingManagerRequestList);
    }

    /** PENDING 상태의 공연 관리자 요청을 승인
     *
     * @param managerRequestId
     */
    @Override
    @PostMapping("/manager-requests/{managerRequestId}/approve")
    public ResponseEntity<Void> approveManagerRequest(@PathVariable("managerRequestId") Long managerRequestId) {
        adminPerformanceService.approveManagerRequest(managerRequestId);
        return ResponseEntity.noContent().build();
    }

    /** PENDING 상태의 공연 관리자 요청을 거부
     *
     * @param managerRequestId
     */
    @Override
    @PostMapping("/manager-requests/{managerRequestId}/reject")
    public ResponseEntity<Void> rejectManagerRequest(@PathVariable("managerRequestId") Long managerRequestId) {
        adminPerformanceService.rejectManagerRequest(managerRequestId);
        return ResponseEntity.noContent().build();
    }
}
