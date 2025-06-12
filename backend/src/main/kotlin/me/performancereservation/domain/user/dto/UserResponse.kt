package me.performancereservation.domain.user.dto

import me.performancereservation.domain.user.enums.Role

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val role: Role
)
