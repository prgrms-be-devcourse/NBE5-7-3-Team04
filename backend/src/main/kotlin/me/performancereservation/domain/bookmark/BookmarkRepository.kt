package me.performancereservation.domain.bookmark

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun existsByUserIdAndPerformanceId(userId: Long, performanceId: Long): Boolean
    fun findByUserIdAndPerformanceId(userId: Long, performanceId: Long): Optional<Bookmark> //Optional 서비스 변환시 수정
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Bookmark>
}
