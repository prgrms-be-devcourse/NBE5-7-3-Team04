package me.performancereservation.api

import me.performancereservation.api.docs.AdminRefundApiDocs
import me.performancereservation.domain.refund.RefundService
import me.performancereservation.domain.refund.dto.RefundDetailResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/refunds")
class AdminRefundController(
    private val refundService: RefundService
) : AdminRefundApiDocs {

    private val log = LoggerFactory.getLogger(AdminRefundController::class.java)
    /*--- ADMIN 요청에 대응 ---*/
    /**
     * 모든 환불 내역 중 특정 status 리스트 반환.
     * /admin/refunds
     * @param status 조회할 환불 상태. null 가능
     * @param pageable 기본값 page 1 size 10
     * @return ResponseEntity<Page></Page><RefundDetailResponse>>
    </RefundDetailResponse> */
    @GetMapping
    override fun getAllRefundDetailsByRefundStatus(
        @RequestParam(required = false) status: String?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<RefundDetailResponse>> {
        log.info(
            "상태별 환불 내역 조회 요청: status={}, page={}, size={}",
            status, pageable.pageNumber, pageable.pageSize
        )

        // status로 조회. 유효성 검사는 서비스측에서 수행
        // status가 null일 경우 전체조회 응답 받음
        return ResponseEntity.ok(refundService.findAllRefundsDetail(status, pageable))
    }

    /**
     * 환불 상태 변경 (환불 승인) PENDING-> CONFIRMED.
     * updateRefundStatus 내부에서 예약 상태도 변경됨 (CANCEL_PENDING-> CANCEL_CONFIRMED).
     * /admin/refunds/{refundId}/confirm
     * @param refundId CONFIRMED로 만들 환불 id
     * @return ResponseEntity<Void>
    </Void> */
    @PatchMapping("/{refundId}/confirm")
    override fun confirmRefund(@PathVariable refundId: Long): ResponseEntity<Void> {
        log.info("환불 승인 요청: refundId={}", refundId)

        refundService.confirmRefund(refundId)
        return ResponseEntity.noContent().build()
    }
}
