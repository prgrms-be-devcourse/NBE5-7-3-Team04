package me.performancereservation.domain.admin.dto.response;

public record PendingManagerRequestPageResponse(
        long id,
        long userId,
        String userName,
        String phoneNumber
) {
}
