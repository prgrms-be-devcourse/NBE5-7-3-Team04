package me.performancereservation.domain.review.repository

import me.performancereservation.domain.review.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByPerformanceIdOrderByCreatedAtDesc(performanceId: Long?, pageable: Pageable?): Page<Review>

    fun existsByUserIdAndPerformanceId(userId: Long, performanceId: Long): Boolean
}
