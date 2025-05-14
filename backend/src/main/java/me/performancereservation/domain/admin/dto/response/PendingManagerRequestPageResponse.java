package me.performancereservation.domain.admin.dto.response;

public record PendingManagerRequestPageResponse(
        Long id,
        Long userId,
        String userName,
        String phoneNumber
) {
}
