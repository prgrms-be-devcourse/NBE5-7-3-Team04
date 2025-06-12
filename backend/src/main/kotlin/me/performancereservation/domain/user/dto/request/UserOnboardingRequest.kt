package me.performancereservation.domain.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UserOnboardingRequest(

    @field:NotBlank(message = "전화번호는 필수 입력값입니다")
    @field:Pattern(
        regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
        message = "올바른 전화번호 형식이 아닙니다"
    )
    val phoneNumber: String,

    @field: Email(message = "올바른 이메일 형식으로 입력하여야 합니다.")
    @field: NotBlank(message = "이메일은 반드시 입력되어야 합니다.")
    val email: String
)
