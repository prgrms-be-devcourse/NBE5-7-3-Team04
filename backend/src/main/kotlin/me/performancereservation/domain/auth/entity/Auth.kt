package me.performancereservation.domain.auth.entity

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity

@Entity
class Auth (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId: Long, // (FK) 유저 ID

    val provider: String, // 써드파티 식별자

    val oauthId: String
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Auth) return false
        if (id == null || other.id == null) return false
        return id == other.id && updatedAt == other.updatedAt
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}