package me.performancereservation.domain.review.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ReviewUpdateRequest(
    @field:NotNull
    @field:NotBlank
    val comment: String
)