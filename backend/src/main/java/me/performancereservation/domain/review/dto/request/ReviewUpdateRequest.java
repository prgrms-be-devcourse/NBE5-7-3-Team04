package me.performancereservation.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequest(
    @NotNull @NotBlank String comment
) {} 