package me.performancereservation.domain.review.dto.respornse;

public record ReviewResponse (
        long id,
        String userName,
        long scheduledId,
        String comment
){}