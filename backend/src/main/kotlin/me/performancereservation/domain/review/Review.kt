package me.performancereservation.domain.review

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.user.entitiy.User

@Entity
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val performanceId: Long,

    val userId: Long,

    @Column(nullable = false, length = 1000)
    var comments: String
) : BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (id == null || other.id == null) return false
        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + updatedAt.hashCode()
        return result
    }

    fun updateComments(comments: String) {
        this.comments = comments
    }
}