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
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bookmark

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (performanceId != other.performanceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + performanceId.hashCode()
        return result
    }
}