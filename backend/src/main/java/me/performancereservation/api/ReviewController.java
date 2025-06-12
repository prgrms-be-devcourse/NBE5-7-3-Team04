package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.api.docs.ReviewApiDocs;
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest;
import me.performancereservation.domain.review.dto.request.ReviewUpdateRequest;
import me.performancereservation.domain.review.dto.respornse.ReviewResponse;
import me.performancereservation.domain.review.service.ReviewService;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewApiDocs {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성
     * @param principal 인증된 사용자
     * @param request 리뷰 작성 요청 DTO
     * @return 200 OK
     */
    @Override
    @PostMapping
    public ResponseEntity<Void> createReview(
        @AuthenticationPrincipal CustomOAuth2User principal,
        @RequestBody ReviewCreateRequest request
    ) {
        reviewService.createReview( principal.user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 공연별 리뷰 목록 조회
     * @param performanceId 공연 ID
     * @return 리뷰 응답 DTO 리스트
     */
    @Override
    @GetMapping("/{performanceId}")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable Long performanceId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByPerformanceId(performanceId, pageable));
    }

    // 리뷰 수정
    @Override
    @PutMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        reviewService.updateReview(reviewId, request, principal.user.getId());

        return ResponseEntity.ok().build();
    }

    // 리뷰 삭제
    @Override
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        reviewService.deleteReview(reviewId, principal.user.getId());

        return ResponseEntity.noContent().build();
    }
}
