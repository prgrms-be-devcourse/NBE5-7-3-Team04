package me.performancereservation.domain.review.mapper

import me.performancereservation.domain.review.Review
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest
import me.performancereservation.domain.review.dto.respornse.ReviewResponse
import me.performancereservation.domain.user.entitiy.User
import org.springframework.stereotype.Component

@Component
class ReviewMapper {
    /**
     * Review 엔티티와 User 엔티티 -> ReviewResponse DTO 변환
     * @param review 리뷰 엔티티
     * @param user   유저 엔티티(이름)
     * @return ReviewResponse DTO
     */
    fun toReviewResponse(review: Review, user: User): ReviewResponse {
        return ReviewResponse(
            review.id!!,
            user.id!!,
            user.name,
            review.comments,
            review.createdAt
        )
    }

    fun toEntity(userId: Long, request: ReviewCreateRequest): Review {
        return Review(
            performanceId = request.performanceId,
            userId = userId,
            comments = request.comment
        )
    }
}
