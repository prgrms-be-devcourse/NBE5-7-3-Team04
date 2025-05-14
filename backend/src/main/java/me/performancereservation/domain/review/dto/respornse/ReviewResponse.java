package me.performancereservation.domain.review.dto.respornse;

public record ReviewResponse (
        Long id,
        String userName,
        Long scheduledId,
        String comment
){}