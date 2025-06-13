package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.performance.dto.performance.PerformanceCreateRequest
import me.performancereservation.domain.performance.dto.performance.PerformanceManagerDetailResponse
import me.performancereservation.domain.performance.dto.performance.PerformanceManagerPageResponse
import me.performancereservation.domain.performance.dto.performance.PerformanceUpdateRequest
import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleRequest
import me.performancereservation.domain.performance.enums.PerformanceStatus
import me.performancereservation.global.exception.ErrorResponse
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import java.time.LocalDateTime

@Tag(name = "Manager Performance API", description = "공연관리자용 공연 관리 API")
interface ManagerPerformanceApiDocs {

    @Operation(summary = "공연 목록 조회", description = "공연관리자가 등록한 공연 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PageableAsQueryParam
    fun getPerformances(
        @ParameterObject pageable: Pageable,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Page<PerformanceManagerPageResponse>>

    @Operation(summary = "공연 상세 조회", description = "공연의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PerformanceManagerDetailResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getPerformanceDetails(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<PerformanceManagerDetailResponse>

    @Operation(summary = "공연 등록", description = "새로운 공연을 등록합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "등록 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (INVALID_REQUEST)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun registerPerformance(
        @Parameter(description = "공연 등록 정보", required = true) request: PerformanceCreateRequest,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Long>

    @Operation(summary = "공연 일정 등록", description = "공연의 일정을 등록합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "등록 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (INVALID_REQUEST)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun registerPerformanceSchedule(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @Parameter(description = "일정 등록 정보", required = true) request: PerformanceScheduleRequest,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Long>

    @Operation(summary = "공연 정보 수정", description = "공연의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (INVALID_REQUEST)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun updatePerformance(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @Parameter(description = "공연 수정 정보", required = true) request: PerformanceUpdateRequest,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void>

    @Operation(summary = "공연 취소", description = "공연을 취소합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "취소 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun cancelPerformance(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void>

    @Operation(summary = "공연 일정 취소", description = "공연의 특정 일정을 취소합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "취소 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "공연 또는 일정을 찾을 수 없음 (PERFORMANCE_NOT_FOUND, PERFORMANCE_SCHEDULE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun cancelPerformanceSchedule(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @Parameter(description = "일정 ID", required = true) performanceScheduleId: Long,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void>

    @Operation(summary = "공연 검색", description = "제목, 공연장, 날짜, 상태로 공연을 검색합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "검색 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (PERMISSION_DENIED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PageableAsQueryParam
    fun searchPerformances(
        @Parameter(description = "공연 제목") title: String?,
        @Parameter(description = "공연장") venue: String?,
        @Parameter(description = "시작 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)") start: LocalDateTime?,
        @Parameter(description = "종료 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)") end: LocalDateTime?,
        @Parameter(description = "공연 상태") status: PerformanceStatus?,
        @ParameterObject pageable: Pageable,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Page<PerformanceManagerPageResponse>>
}