package me.performancereservation.domain.review.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.review.Review;
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest;
import me.performancereservation.domain.review.dto.respornse.ReviewResponse;
import me.performancereservation.domain.review.mapper.ReviewMapper;
import me.performancereservation.domain.review.repository.ReviewRepository;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    /**
     * 리뷰 작성
     * @param userId 작성자
     * @param request 리뷰 작성 요청 DTO
     */
    @Transactional
    public void createReview(Long userId, ReviewCreateRequest request) {
        Review review = Review.builder()
                .performanceId(request.performanceId())
                .scheduleId(request.scheduledId())
                .userId(userId)
                .comments(request.comments())
                .build();
        reviewRepository.save(review);
    }

    /**
     * 공연별 리뷰 목록 조회
     * @param performanceId 공연 ID
     * @return 리뷰 응답 DTO 리스트
     */
    @Transactional
    public Page<ReviewResponse> getReviewsByPerformanceId(Long performanceId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByPerformanceIdOrderByCreatedAtDesc(performanceId,pageable);
        //유저 id 모아서 한번에 유조 조회하고 맵핑!
        List<Long> userIds = reviews.stream().map(Review::getUserId).distinct().toList();
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));

        //맵퍼로 변환하기
        return reviews.map(review -> reviewMapper.toReviewResponse(review, userMap.get(review.getUserId())));
    }
}
