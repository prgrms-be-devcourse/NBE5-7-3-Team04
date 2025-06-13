package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.bookmark.dto.BookmarkedPerformancePageResponse
import me.performancereservation.global.exception.ErrorResponse
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal

@Tag(name = "Bookmark API", description = "북마크 관련 API")
interface BookmarkApiDocs {
    @Operation(summary = "공연 북마크", description = "공연을 북마크합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "북마크 성공"),
            ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "404",
            description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "409",
            description = "이미 북마크된 공연 (DUPLICATE_BOOKMARK)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun performanceBookmark(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User,
        @Parameter(description = "공연 ID", required = true) performanceId: Long
    ): ResponseEntity<Void>

    @Operation(summary = "북마크 취소", description = "공연 북마크를 취소합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "취소 성공"),
            ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "404",
            description = "북마크를 찾을 수 없음 (BOOKMARK_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun performanceBookmarkCancel(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User,
        @Parameter(description = "공연 ID", required = true) performanceId: Long
    ): ResponseEntity<Void>

    @Operation(summary = "북마크 목록 조회", description = "사용자가 북마크한 공연 목록을 조회합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "조회 성공"), ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    @PageableAsQueryParam
    fun getBookmarkedPerformances(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal principal: CustomOAuth2User,
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<BookmarkedPerformancePageResponse>>
}