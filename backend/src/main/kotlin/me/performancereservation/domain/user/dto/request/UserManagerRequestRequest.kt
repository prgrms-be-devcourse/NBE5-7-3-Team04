package me.performancereservation.domain.user.dto.request

import jakarta.validation.constraints.NotBlank

/** 사용자의 공연 관리자 신청 요청
 *
 * @param organizationName      개인 혹은 단체명
 * @param organizationContact   연락처
 * @param experience            공연 경험
 * @param reason                신청 사유
 */
data class UserManagerRequestRequest(
    @field: NotBlank(message = "단체명은 반드시 입력되어야 합니다.")
    val organizationName: String,
    @field: NotBlank(message = "단체 연락처는 반드시 입력되어야 합니다.")
    val organizationContact: String,
    @field: NotBlank(message = "공연 경험은 반드시 입력되어야 합니다.")
    val experience: String,
    @field: NotBlank(message = "신청 사유는 반드시 입력되어야 합니다.")
    val reason: String
)