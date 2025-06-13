package me.performancereservation.domain.bookmark

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun existsByUserIdAndPerformanceId(userId: Long, performanceId: Long): Boolean
    fun findByUserIdAndPerformanceId(userId: Long, performanceId: Long): Bookmark?
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Bookmark>
}
