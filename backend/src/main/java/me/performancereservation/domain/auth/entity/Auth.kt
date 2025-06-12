package me.performancereservation.domain.auth.entity

import jakarta.persistence.*
import me.performancereservation.domain.common.BaseEntity

@Entity
class Auth (

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId: Long, // (FK) 유저 ID

    val provider: String, // 써드파티 식별자

    val oauthId: String
) : BaseEntity()