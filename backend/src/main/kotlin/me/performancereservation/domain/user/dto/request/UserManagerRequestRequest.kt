package me.performancereservation.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/** 사용자의 공연 관리자 신청 요청
 *
 * @param organizationName      개인 혹은 단체명
 * @param organizationContact   연락처
 * @param experience            공연 경험
 * @param reason                신청 사유
 */
data class UserManagerRequestRequest(
    @field:NotBlank(message = "단체명은 필수 입력값입니다")
    val organizationName: String,

    @field:Pattern(
        regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
        message = "올바른 연락처 형식이 아닙니다"
    )
    val organizationContact: String,

    val experience: String,

    @field:NotBlank(message = "신청 사유는 필수 입력값입니다")
    val reason: String
)
