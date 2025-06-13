package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.api.docs.AdminRefundApiDocs;
import me.performancereservation.domain.refund.RefundService;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/refunds")
public class AdminRefundController implements AdminRefundApiDocs {

    private final RefundService refundService;

    /*--- ADMIN 요청에 대응 ---*/

    /**
     * 모든 환불 내역 중 특정 status 리스트 반환.
     * /admin/refunds
     * @param status 조회할 환불 상태. null 가능
     * @param pageable 기본값 page 1 size 10
     * @return ResponseEntity<Page<RefundDetailResponse>>
     */
    @Override
    @GetMapping
    public ResponseEntity<Page<RefundDetailResponse>> getAllRefundDetailsByRefundStatus(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("상태별 환불 내역 조회 요청: status={}, page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        // status로 조회. 유효성 검사는 서비스측에서 수행
        // status가 null일 경우 전체조회 응답 받음
        return ResponseEntity.ok(refundService.findAllRefundsDetail(status, pageable));
    }

    /**
     * 환불 상태 변경 (환불 승인) PENDING-> CONFIRMED.
     * updateRefundStatus 내부에서 예약 상태도 변경됨 (CANCEL_PENDING-> CANCEL_CONFIRMED).
     * /admin/refunds/{refundId}/confirm
     * @param refundId CONFIRMED로 만들 환불 id
     * @return ResponseEntity<Void>
     */
    @Override
    @PatchMapping("/{refundId}/confirm")
    public ResponseEntity<Void> confirmRefund(@PathVariable Long refundId) {
        log.info("환불 승인 요청: refundId={}", refundId);

        refundService.confirmRefund(refundId);
        return ResponseEntity.noContent().build();
    }
}
