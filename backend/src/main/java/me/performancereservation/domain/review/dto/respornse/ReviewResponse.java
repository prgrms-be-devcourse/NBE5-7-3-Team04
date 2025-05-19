package me.performancereservation.domain.review.dto.respornse;

import java.time.LocalDateTime;

public record ReviewResponse (
        long id,
        long userId,
        String userName,
        String comment,
        LocalDateTime createdAt
){}