package me.performancereservation.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest (
        @NotNull Long performanceId,
        @NotNull @NotBlank String comment
){}
