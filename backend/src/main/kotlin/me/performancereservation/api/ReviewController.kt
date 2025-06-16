package me.performancereservation.api

import io.swagger.v3.oas.annotations.Parameter
import me.performancereservation.domain.review.dto.response.ReviewResponse
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest
import me.performancereservation.domain.review.dto.request.ReviewUpdateRequest
import me.performancereservation.api.docs.ReviewApiDocs
import me.performancereservation.domain.review.service.ReviewService
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(
    private val reviewService: ReviewService
) : ReviewApiDocs {

    /**
     * 리뷰 작성
     * @param principal 인증된 사용자
     * @param request 리뷰 작성 요청 DTO
     * @return 200 OK
     */
    @PostMapping
    override fun createReview(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @RequestBody request: ReviewCreateRequest
    ): ResponseEntity<Void> {
        principal.user.id?.let { reviewService.createReview(it, request) }
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /**
     * 공연별 리뷰 목록 조회
     * @param performanceId 공연 ID
     * @return 리뷰 응답 DTO 리스트
     */
    @GetMapping("/{performanceId}")
    override fun getReviews(
        @Parameter(description = "공연 ID", required = true) performanceId: Long,
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<ReviewResponse>> =
        ResponseEntity.ok(pageable?.let { reviewService.getReviewsByPerformanceId(performanceId, it) })


    // 리뷰 수정
    @PutMapping("/{reviewId}")
    override fun updateReview(
        @PathVariable reviewId: Long,
        @RequestBody request: ReviewUpdateRequest,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void> {
        principal.user.id?.let { reviewService.updateReview(reviewId, request, it) }
        return ResponseEntity.ok().build()
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    override fun deleteReview(
        @PathVariable reviewId: Long,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void> {
        principal.user.id?.let { reviewService.deleteReview(reviewId, it) }
        return ResponseEntity.noContent().build()
    }
}