package me.performancereservation.domain.review.dto.response

import java.time.LocalDateTime

data class ReviewResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val comment: String,
    val createdAt: LocalDateTime
) 