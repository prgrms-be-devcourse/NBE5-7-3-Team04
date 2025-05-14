package me.performancereservation.domain.user.dto;

public record UserOnboardingRequest (
        String phoneNumber,
        String email
){}
