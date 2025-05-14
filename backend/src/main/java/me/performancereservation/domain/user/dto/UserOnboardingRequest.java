package me.performancereservation.domain.user.dto;

public record UserOnboardingRequest (
        String phoneNumber,
        String email //카카오 로그인인 경우에만 입력 받기.
){}
