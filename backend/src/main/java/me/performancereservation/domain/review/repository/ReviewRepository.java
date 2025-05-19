package me.performancereservation.domain.review.repository;

import me.performancereservation.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    //리뷰 페이징
    Page<Review> findByPerformanceIdOrderByCreatedAtDesc(Long performanceId, Pageable pageable);

    boolean existsByUserIdAndPerformanceId(Long userId, Long performanceId);
}
