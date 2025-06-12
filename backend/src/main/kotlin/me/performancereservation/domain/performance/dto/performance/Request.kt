package me.performancereservation.domain.performance.dto.performance

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class PerformanceCreateRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val venue: String,
    @field:Positive val price: Int,
    @field:Positive val totalSeats: Int,
    @field:NotBlank val category: String,
    @field:NotNull val startDate: LocalDateTime,
    @field:NotNull val endDate: LocalDateTime,
    @field:NotBlank val description: String,
    val fileId: Long? = null
)

data class PerformanceUpdateRequest(
    @field:NotNull val fileId: Long,
    @field:NotBlank val description: String
)
