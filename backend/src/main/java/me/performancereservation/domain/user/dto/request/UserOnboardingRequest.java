package me.performancereservation.domain.user.dto.request;

public record UserOnboardingRequest (
        String phoneNumber,
        String email
){}
