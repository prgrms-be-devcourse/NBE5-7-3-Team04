package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest
import me.performancereservation.domain.review.dto.request.ReviewUpdateRequest
import me.performancereservation.domain.review.dto.response.ReviewResponse
import me.performancereservation.global.exception.ErrorResponse
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal

@Tag(name = "Review API", description = "리뷰 관련 API")
interface ReviewApiDocs {

    @Operation(summary = "리뷰 작성", description = "공연에 대한 리뷰를 작성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "작성 성공"),
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
                responseCode = "404",
                description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun createReview(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User,
        @Parameter(description = "리뷰 작성 정보", required = true) request: ReviewCreateRequest
    ): ResponseEntity<Void>

    @Operation(summary = "리뷰 목록 조회", description = "공연의 리뷰 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PageableAsQueryParam
    fun getReviews(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<ReviewResponse>>

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
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
                description = "권한 없음 (FORBIDDEN)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "리뷰를 찾을 수 없음 (REVIEW_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun updateReview(
        @Parameter(description = "리뷰 ID", required = true) reviewId: Long,
        @Parameter(description = "리뷰 수정 정보", required = true) request: ReviewUpdateRequest,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void>

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "권한 없음 (FORBIDDEN)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "리뷰를 찾을 수 없음 (REVIEW_NOT_FOUND)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    fun deleteReview(
        @Parameter(description = "리뷰 ID", required = true) reviewId: Long,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void>
}