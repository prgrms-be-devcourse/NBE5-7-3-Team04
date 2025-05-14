package me.performancereservation.domain.review.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.review.Review;
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest;
import me.performancereservation.domain.review.dto.respornse.ReviewResponse;
import me.performancereservation.domain.review.mapper.ReviewMapper;
import me.performancereservation.domain.review.repository.ReviewRepository;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.repository.UserRepository;
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

    //리뷰 작성
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

    //리뷰 리스트 조회
    @Transactional
    public List<ReviewResponse> getReviewsByPerformanceId(Long performanceId) {
        List<Review> reviews = reviewRepository.findByPerformanceIdOrderByCreatedAtDesc(performanceId);
        //유저 id 모아서 한번에 유조 조회하고 맵핑!
        List<Long> userIds = reviews.stream().map(Review::getUserId).distinct().toList();
        List<User> users = userRepository.findAllById(userIds);

        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));

        return reviews.stream()
                .map(review -> reviewMapper.toReviewResponse(review, userMap.get(review.getUserId()))).toList();
    }
}
