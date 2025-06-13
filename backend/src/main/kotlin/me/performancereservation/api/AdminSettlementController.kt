package me.performancereservation.api

import me.performancereservation.api.docs.AdminSettlementApiDocs
import me.performancereservation.domain.settlement.SettlementService
import me.performancereservation.domain.settlement.dto.SettlementResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/settlements")
class AdminSettlementController(
    private val settlementService: SettlementService
) : AdminSettlementApiDocs {

    private val log = LoggerFactory.getLogger(AdminSettlementController::class.java)

    /**
     * api/v1/admin/settlements/{settlementId}/confirm 으로 ADMIN이 승인진행
     * @param settlementId 승인할 정산 id
     * @return ResponseEntity<SettlementResponse> 필드 변경된 정산 결과
    </SettlementResponse> */
    @PatchMapping("/{settlementId}/confirm")
    override fun confirmSettlement(@PathVariable settlementId: Long): ResponseEntity<SettlementResponse> {
        val response = settlementService.confirmSettlement(settlementId)
        return ResponseEntity.ok(response)
    }

    /**
     * api/v1/admin/settlements?status=__ 으로 모든 유저의 정산 조회.
     * status == null이면 전체 조회.
     * status가 유효하면 해당 status인 정산만 조회.
     * @param status PENDING 혹은 CONFIRMED여야 한다
     * @return ResponseEntity<Page></Page><SettlementResponse>> status로 필터링한 모든 유저의 정산목록 Page
    </SettlementResponse> */
    @GetMapping
    override fun getAllSettlementsWithStatus(
        @RequestParam(required = false) status: String,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<SettlementResponse>> {
        log.info(
            "ADMIN 정산 내역 조회 요청: status={}, page={}, size={}",
            status, pageable.pageNumber, pageable.pageSize
        )

        // 서비스 내에서 status string 유효성 검사 진행
        val response =
            settlementService.findAllSettlementsByStatus(status, pageable)

        return ResponseEntity.ok(response)
    }
}
