package me.performancereservation.domain.user.entitiy

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity
import me.performancereservation.domain.user.enums.Role

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var email: String,

    val name: String,

    var phoneNumber: String,

    @Enumerated(EnumType.STRING)
    var role: Role
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

    fun updatePhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
    }

    fun updateEmail(email: String) {
        this.email = email
    }

    fun promoteManager() {
        this.role = Role.MANAGER
    }
}