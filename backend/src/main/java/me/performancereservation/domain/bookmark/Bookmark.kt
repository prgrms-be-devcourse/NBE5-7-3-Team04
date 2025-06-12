package me.performancereservation.domain.bookmark

import jakarta.persistence.*
import lombok.Builder
import me.performancereservation.domain.common.BaseEntity

@Entity
class Bookmark(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,       // 찜 ID

    val userId: Long,           // (FK)사용자 ID
    val performanceId: Long     // (FK)공연 ID
) : BaseEntity()