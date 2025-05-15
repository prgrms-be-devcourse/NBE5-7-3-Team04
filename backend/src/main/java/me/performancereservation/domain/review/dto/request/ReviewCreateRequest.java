package me.performancereservation.domain.review.dto.request;

public record ReviewCreateRequest (
        Long performanceId,
        Long scheduledId,
        String comments
){}
