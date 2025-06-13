package me.performancereservation.domain.review.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ReviewCreateRequest(
    @field:NotNull
    val performanceId: Long,

    @field:NotNull
    @field:NotBlank
    val comment: String
)