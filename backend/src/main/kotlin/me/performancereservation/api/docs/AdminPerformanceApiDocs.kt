package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse
import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse
import me.performancereservation.domain.performance.enums.PerformanceStatus
import me.performancereservation.global.exception.ErrorResponse
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Admin Performance API", description = "관리자용 공연 관련 API")
interface AdminPerformanceApiDocs {
    @Operation(summary = "대기 중인 공연 목록 조회", description = "PENDING 상태의 공연과 포스터, 스케줄, 공연 관리자 정보를 묶어 반환합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "조회 성공"), ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    @PageableAsQueryParam
    fun performances(
        @RequestParam(required = false) status: PerformanceStatus?,
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<PendingPerformancePageResponse>>

    @Operation(summary = "공연 승인", description = "PENDING 상태의 공연을 승인합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "승인 성공"), ApiResponse(
            responseCode = "400",
            description = "PENDING 상태가 아닌 공연 (PERFORMANCE_STATUS_NOT_PENDING)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
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
            description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun confirmPerformance(
        @Parameter(description = "공연 ID", required = true) performanceId: Long
    ): ResponseEntity<Void>

    @Operation(summary = "공연 거부", description = "PENDING 상태의 공연을 거부합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "거부 성공"), ApiResponse(
            responseCode = "400",
            description = "PENDING 상태가 아닌 공연 (PERFORMANCE_STATUS_NOT_PENDING)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
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
            description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun rejectPerformance(
        @Parameter(description = "공연 ID", required = true) performanceId: Long
    ): ResponseEntity<Void>

    @Operation(summary = "대기 중인 공연 관리자 요청 목록 조회", description = "PENDING 상태의 공연 관리자 요청과 해당하는 유저의 정보를 묶어 반환합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "조회 성공"), ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    @PageableAsQueryParam
    fun pendingManagerRequests(
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<PendingManagerRequestPageResponse>>

    @Operation(summary = "공연 관리자 요청 승인", description = "PENDING 상태의 공연 관리자 요청을 승인하고 사용자의 역할을 MANAGER로 변경합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "승인 성공"), ApiResponse(
            responseCode = "400",
            description = "PENDING 상태가 아닌 요청 (MANAGER_REQUEST_STATUS_NOT_PENDING)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
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
            description = "요청을 찾을 수 없음 (MANAGER_REQUEST_NOT_FOUND) 또는 사용자를 찾을 수 없음 (USER_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun approveManagerRequest(
        @Parameter(description = "관리자 요청 ID", required = true) managerRequestId: Long
    ): ResponseEntity<Void>

    @Operation(summary = "공연 관리자 요청 거부", description = "PENDING 상태의 공연 관리자 요청을 거부합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "거부 성공"), ApiResponse(
            responseCode = "400",
            description = "PENDING 상태가 아닌 요청 (MANAGER_REQUEST_STATUS_NOT_PENDING)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
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
            description = "요청을 찾을 수 없음 (MANAGER_REQUEST_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun rejectManagerRequest(
        @Parameter(description = "관리자 요청 ID", required = true) managerRequestId: Long
    ): ResponseEntity<Void>
}