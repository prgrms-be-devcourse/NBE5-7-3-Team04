package me.performancereservation.domain.review.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.review.Review;
import me.performancereservation.domain.review.dto.request.ReviewCreateRequest;
import me.performancereservation.domain.review.dto.respornse.ReviewResponse;
import me.performancereservation.domain.review.mapper.ReviewMapper;
import me.performancereservation.domain.review.repository.ReviewRepository;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.ErrorCode;
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
    private final PerformanceRepository performanceRepository;
    private final ReservationRepository reservationRepository;
    private final PerformanceScheduleRepository performanceScheduleRepository;
    private Review review;


    /**
     * 리뷰 작성
     * @param userId 작성자
     * @param request 리뷰 작성 요청 DTO
     */
    @Transactional
    public void createReview(Long userId, ReviewCreateRequest request) {

        checkExistPerformance(request.performanceId());
        checkExistSchedule(request.performanceId(), request.scheduledId());
        checkValidateUser(userId, request.performanceId());

        reviewMapper.toEntity(userId, request);
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

    //찾아온 후 비어있는지 확인하는 방법보단 단순 존재 여부를 확인하기 위해 exist를 사용하였습니다.
    private void checkExistPerformance(Long performanceId) {
        performanceRepository.findById(performanceId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("존재하지 않는 공연입니다."));
    }

    private void checkExistSchedule(Long performanceId, Long scheduleId) {
        boolean exists = performanceScheduleRepository.existsByIdAndPerformanceId(performanceId, scheduleId);
        if (!exists) {
            throw ErrorCode.INVALID_SCHEDULE.domainException("해당 공연에 속하지 않는 회차입니다.");
        }
    }

    private void checkValidateUser(Long userId, Long scheduleId) {
        if (!reservationRepository.existsByUserIdAndScheduleId(userId, scheduleId)) {
            throw ErrorCode.UNAUTHORIZED_REVIEW.domainException("예매 내역이 없는 공연 회차에는 리뷰를 작성할 수 없습니다.");
        }
        if (reviewRepository.existsByUserIdAndScheduleId(userId, scheduleId)) {
            throw ErrorCode.DUPLICATE_REVIEW.domainException("이미 리뷰를 작성하셨습니다.");
        }
    }
}
