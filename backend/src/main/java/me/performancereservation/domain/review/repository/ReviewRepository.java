package me.performancereservation.domain.review.repository;

import me.performancereservation.domain.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
