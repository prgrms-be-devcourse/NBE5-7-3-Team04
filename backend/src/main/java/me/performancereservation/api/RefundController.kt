package me.performancereservation.api

import jakarta.validation.Valid
import me.performancereservation.api.docs.RefundApiDocs
import me.performancereservation.domain.refund.RefundService
import me.performancereservation.domain.refund.dto.RefundDetailResponse
import me.performancereservation.domain.refund.dto.RefundResponse
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest
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

private val log = LoggerFactory.getLogger(RefundController::class.java)

@RestController
@RequestMapping("/api/v1/refunds")
class RefundController(
    private val refundService: RefundService
) : RefundApiDocs {

    /*--- USER 요청에 대응 ---*/
    /**
     * 본인 id와 일치하는 모든 환불내역 리스트 반환.
     * /refunds/me
     * @param authentication 현재 로그인한 인증 정보
     * @param pageable 기본값
     * @return ResponseEntity<Page></Page><RefundDetailResponse>>
    </RefundDetailResponse> */
    @GetMapping("/me")
    override fun getAllRefundDetailsWithUserId(
        @AuthenticationPrincipal authentication: CustomOAuth2User,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<RefundDetailResponse>> {
        val userId = authentication.user.id

        log.info(
            "사용자 환불 내역 조회 요청: userId={}, page={}, size={}",
            userId, pageable.pageNumber, pageable.pageSize
        )

        return ResponseEntity.status(HttpStatus.OK)
            .body(refundService.findAllRefundsDetailByUserId(userId, pageable))
    }

    /** 예매 내역 환불 정보용
     *
     * @param authentication
     * @return RefundResponse
     */
    @GetMapping("/{reservationId}")
    override fun getRefund(
        @AuthenticationPrincipal authentication: CustomOAuth2User,
        @PathVariable reservationId: Long
    ): ResponseEntity<RefundResponse> {
        val userId = authentication.user.id


        return ResponseEntity.status(HttpStatus.OK)
            .body(refundService.findRefundByUserId(userId, reservationId))
    }

    /**
     * 계좌, 은행명, 입금자명을 dto로 받아서 설정.
     * /refunds
     * @param request
     * @return
     */
    @PatchMapping
    override fun updateBankInfo(
        @AuthenticationPrincipal authentication: CustomOAuth2User,
        @RequestBody @Valid request: UpdateBankInfoRequest
    ): ResponseEntity<Void> {
        log.info("은행 정보 호출")
        val userId = authentication.user.id

        log.info("refund = {}", request.toString())
        refundService.updateBankInfo(userId, request)
        return ResponseEntity.status(HttpStatus.OK).build()
    }
}
