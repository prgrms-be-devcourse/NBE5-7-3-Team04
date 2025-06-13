package me.performancereservation.api

import jakarta.validation.Valid
import me.performancereservation.api.docs.SettlementApiDocs
import me.performancereservation.domain.settlement.SettlementService
import me.performancereservation.domain.settlement.dto.SettlementRequest
import me.performancereservation.domain.settlement.dto.SettlementResponse
import me.performancereservation.domain.settlement.dto.SettlementUpdateRequest
import me.performancereservation.domain.settlement.dto.SettlementUpdateResponse
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/managers/settlements")
class ManagerSettlementController(
    private val settlementService: SettlementService
) : SettlementApiDocs {

    private val log = LoggerFactory.getLogger(ManagerSettlementController::class.java)

    /**
     * api/v1/managers/settlements/register 로 공연관리자가 정산생성 요청
     * @param request SettlementRequest DTO
     * @return 생성된 정산 Long id
     */
    @PostMapping("/register")
    override fun createSettlement(
        @Valid @RequestBody request: SettlementRequest
    ): ResponseEntity<Long> {
        val settlementId = settlementService.createSettlement(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementId)
    }

    /**
     * /api/v1/managers/settlements/edit
     * @param request 정산id, 은행, 계좌 정보
     * @return 정산id, 공연id, 업데이트된 은행과 계좌 정보
     */
    @PatchMapping("/edit")
    override fun updateSettlementBankInfo(
        @Valid @RequestBody request: SettlementUpdateRequest
    ): ResponseEntity<SettlementUpdateResponse> {
        log.info("[editSettlement] 요청: {}", request)
        val settlementUpdateResponse = settlementService.updateSettlement(request)
        return ResponseEntity.ok(settlementUpdateResponse)
    }

    // 본인 userid로 전체 정산목록 조회 → return Page<SettlementResponse >
    /**
     * 현재 로그인된 userid로 정산목록 조회.
     * /api/v1/managers/settlements/me
     * @param authentication 현재 로그인 한 OAuth2User의 principal
     * @param pageable 조회할 페이지
     * @return ResponseEntity<Page></Page><SettlementResponse>> 유저id로 조회한 정산목록 Page
    </SettlementResponse> */
    @GetMapping("/me")
    override fun getAllSettlementsWithUserId(
        @AuthenticationPrincipal authentication: CustomOAuth2User,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<SettlementResponse>> {
        val userId = authentication.user.id
        log.info(
            "사용자 정산 내역 조회 요청: userId={}, page={}, size={}",
            userId, pageable.pageNumber, pageable.pageSize
        )

        return ResponseEntity.ok(settlementService.findAllSettlementsWithUserId(userId, pageable))
    }

    /**
     * /api/v1/managers/settlements?performanceId={id}
     * 해당 공연id로 만들어진 정산이 있는지 확인.
     * null을 받았다면 정산이 만들어진 적 없음.
     * @param performanceId 정산 생성 여부를 확인할 공연id
     * @return 공연id로 생성된 정산의 id
     */
    @GetMapping
    override fun findSettlementIdByPerformanceId(@RequestParam performanceId: Long): ResponseEntity<Long> {
        return ResponseEntity.ok(settlementService.findSettlementIdByPerformanceId(performanceId))
    }
}
