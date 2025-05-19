package me.performancereservation.domain.review.dto.respornse;

import java.time.LocalDateTime;

public record ReviewResponse (
        long id,
        String userName,
        long scheduledId,
        String comment,
        LocalDateTime createdAt
){}