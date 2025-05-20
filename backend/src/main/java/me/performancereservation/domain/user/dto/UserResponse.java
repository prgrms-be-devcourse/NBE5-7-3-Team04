package me.performancereservation.domain.user.dto;

import me.performancereservation.domain.user.enums.Role;

public record UserResponse(Long id, String email, String name, String phoneNumber ,Role role) {
}
