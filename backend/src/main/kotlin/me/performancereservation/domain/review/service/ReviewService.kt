package me.performancereservation.domain.review.service

import me.performancereservation.domain.performance.repository.PerformanceRepository
import me.performancereservation.domain.reservation.ReservationRepository
import me.performancereservation.domain.review.Review
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest
import me.performancereservation.domain.review.dto.request.ReviewUpdateRequest
import me.performancereservation.domain.review.dto.response.ReviewResponse
import me.performancereservation.domain.review.mapper.ReviewMapper
import me.performancereservation.domain.review.repository.ReviewRepository
import me.performancereservation.domain.user.repository.UserRepository
import me.performancereservation.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val reviewMapper: ReviewMapper,
    private val performanceRepository: PerformanceRepository,
    private val reservationRepository: ReservationRepository
) {
    /**
     * 리뷰 작성
     * @param userId 작성자
     * @param request 리뷰 작성 요청 DTO
     */
    @Transactional
    fun createReview(userId: Long, request: ReviewCreateRequest) {
        checkExistPerformance(request.performanceId)
        checkValidateUser(userId, request.performanceId)

        val review = reviewMapper.toEntity(userId, request)
        reviewRepository.save(review)
    }

    /**
     * 공연별 리뷰 목록 조회
     * @param performanceId 공연 ID
     * @return 리뷰 응답 DTO 리스트
     */
    @Transactional
    fun getReviewsByPerformanceId(performanceId: Long, pageable: Pageable): Page<ReviewResponse> {
        val reviews = reviewRepository.findByPerformanceIdOrderByCreatedAtDesc(performanceId, pageable)
        //유저 id 모아서 한번에 유조 조회하고 맵핑!
        val userIds = reviews.map { it.userId }.distinct()
        val users = userRepository.findAllById(userIds)
        val userMap = users.associateBy { it.id }

        //맵퍼로 변환하기
        return reviews.map { review: Review ->
            reviewMapper.toReviewResponse(
                review,
                userMap[review.userId]!!
            )
        }
    }

    // 리뷰 수정
    @Transactional
    fun updateReview(reviewId: Long, request: ReviewUpdateRequest, userId: Long) {
        val review = reviewRepository.findByIdOrNull(reviewId)
            ?: throw ErrorCode.REVIEW_NOT_FOUND.domainException("리뷰를 찾을 수 없습니다.")

        if (review.userId != userId) {
            throw ErrorCode.UNAUTHORIZED.domainException("리뷰를 수정할 권한이 없습니다.")
        }

        review.updateComments(request.comment)
    }

    @Transactional
    fun deleteReview(reviewId: Long, userId: Long) {
        val review = reviewRepository.findByIdOrNull(reviewId)
            ?: throw ErrorCode.REVIEW_NOT_FOUND.domainException("리뷰를 찾을 수 없습니다.")

        if (review.userId != userId) {
            throw ErrorCode.UNAUTHORIZED.domainException("리뷰를 삭제할 권한이 없습니다.")
        }

        reviewRepository.delete(review)
    }

    private fun checkExistPerformance(performanceId: Long) {
        performanceRepository.findByIdOrNull(performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("존재하지 않는 공연입니다.")
    }

    private fun checkValidateUser(userId: Long, performanceId: Long) {
        if (!reservationRepository.existsByUserIdAndPerformanceId(userId, performanceId)) {
            throw ErrorCode.UNAUTHORIZED_REVIEW.domainException("예매 내역이 없는 공연 회차에는 리뷰를 작성할 수 없습니다.")
        }
        if (reviewRepository.existsByUserIdAndPerformanceId(userId, performanceId)) {
            throw ErrorCode.DUPLICATE_REVIEW.domainException("이미 리뷰를 작성하셨습니다.")
        }
    }
}
