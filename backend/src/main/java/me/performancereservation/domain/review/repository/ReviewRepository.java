package me.performancereservation.domain.review.repository;

import me.performancereservation.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
        SELECT r FROM Review r
        WHERE r.performanceId = :performanceId
        ORDER BY r.createdAt DESC
    """)
    List<Review> findByPerformanceIdOrderByCreatedAtDesc(Long performanceId);
}
