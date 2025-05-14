package me.performancereservation.domain.review.mapper;

import me.performancereservation.domain.review.Review;
import me.performancereservation.domain.review.dto.respornse.ReviewResponse;
import me.performancereservation.domain.user.entitiy.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    /**
     * Review 엔티티와 User 엔티티 -> ReviewResponse DTO 변환
     * @param review 리뷰 엔티티
     * @param user   유저 엔티티(이름)
     * @return ReviewResponse DTO
     */
    public ReviewResponse toReviewResponse(Review review, User user) {
        return new ReviewResponse(
                review.getId(),
                user.getName(),
                review.getScheduleId(),
                review.getComments()
        );
    }
}
