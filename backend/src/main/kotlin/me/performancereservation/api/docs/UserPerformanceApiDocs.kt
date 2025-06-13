package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.performance.dto.performance.PerformanceDetailResponse
import me.performancereservation.domain.performance.dto.performance.PerformancePageResponse
import me.performancereservation.domain.performance.enums.PerformanceCategory
import me.performancereservation.global.exception.ErrorResponse
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import java.time.LocalDateTime

@Tag(name = "User Performance API", description = "사용자용 공연 조회 API")
interface UserPerformanceApiDocs {

    @Operation(summary = "공연 목록 조회", description = "메인 화면에 표시될 공연 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    @PageableAsQueryParam
    fun getPerformanceList(
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<PerformancePageResponse>>

    @Operation(summary = "공연 상세 정보 조회", description = "공연의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PerformanceDetailResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun getPerformanceDetail(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User?
    ): ResponseEntity<PerformanceDetailResponse>

    @Operation(summary = "공연 검색", description = "제목, 공연장, 날짜, 카테고리로 공연을 검색합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "검색 성공")
        ]
    )
    @PageableAsQueryParam
    fun searchPerformanceList(
        @Parameter(description = "공연 제목") title: String?,
        @Parameter(description = "공연장") venue: String?,
        @Parameter(description = "시작 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)") start: LocalDateTime?,
        @Parameter(description = "종료 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)") end: LocalDateTime?,
        @Parameter(description = "공연 카테고리") category: PerformanceCategory?,
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<PerformancePageResponse>>
}