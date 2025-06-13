package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.refund.dto.RefundDetailResponse
import me.performancereservation.domain.refund.dto.RefundResponse
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest
import me.performancereservation.global.exception.ErrorResponse
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Refund API", description = "환불 관련 API")
interface RefundApiDocs {

    @Operation(summary = "환불 내역 조회", description = "사용자의 모든 환불 내역을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PageableAsQueryParam
    fun getAllRefundDetailsWithUserId(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal authentication: CustomOAuth2User,
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<RefundDetailResponse>>

    @Operation(summary = "환불 계좌 정보 수정", description = "환불을 받을 계좌 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun updateBankInfo(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal authentication: CustomOAuth2User,
        @Parameter(description = "계좌 정보", required = true) request: UpdateBankInfoRequest
    ): ResponseEntity<Void>

    @Operation(summary = "예약 환불 정보 조회", description = "예약 상세 페이지 환불 정보를 출력합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "조회 실패 (환불 정보 없음)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getRefund(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal authentication: CustomOAuth2User,
        @Parameter(description = "예약 ID", required = true) @PathVariable reservationId: Long
    ): ResponseEntity<RefundResponse>
}