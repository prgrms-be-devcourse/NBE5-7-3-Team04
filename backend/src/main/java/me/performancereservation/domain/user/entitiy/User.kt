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