package me.performancereservation.domain.review.dto.request

import jakarta.validation.constraints.NotBlank

data class ReviewUpdateRequest(
    @field:NotBlank
    val comment: String
)