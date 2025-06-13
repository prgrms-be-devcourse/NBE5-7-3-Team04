package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.settlement.dto.SettlementResponse
import me.performancereservation.global.exception.ErrorResponse
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity

@Tag(name = "Admin Settlement API", description = "관리자용 정산 관련 API")
interface AdminSettlementApiDocs {
    @Operation(summary = "정산 승인", description = "대기 중인 정산을 승인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
            responseCode = "200",
            description = "승인 성공",
            content = [Content(schema = Schema(implementation = SettlementResponse::class))]
        ), ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "404",
            description = "정산을 찾을 수 없음 (SETTLEMENT_NOT_FOUND) 또는 공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun confirmSettlement(
        @Parameter(description = "정산 ID", required = true) settlementId: Long
    ): ResponseEntity<SettlementResponse>

    @Operation(summary = "정산 목록 조회", description = "모든 정산 목록을 조회합니다. 상태별 필터링이 가능합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 정산 상태 (INVALID_SETTLEMENT_STATUS)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    @PageableAsQueryParam
    fun getAllSettlementsWithStatus(
        @Parameter(description = "정산 상태 (PENDING 또는 CONFIRMED). null인 경우 전체 조회") status: String,
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<SettlementResponse>>
}