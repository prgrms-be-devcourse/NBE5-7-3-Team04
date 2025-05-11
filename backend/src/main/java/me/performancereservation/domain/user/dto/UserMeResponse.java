package me.performancereservation.domain.user.dto;

import me.performancereservation.domain.user.enums.Role;

public record UserMeResponse(Long id, String email, String name, Role role) {
}
