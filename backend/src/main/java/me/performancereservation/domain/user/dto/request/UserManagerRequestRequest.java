package me.performancereservation.domain.user.dto.request;

/** 사용자의 공연 관리자 신청 요청
 *
 * @param organizationName      개인 혹은 단체명
 * @param organizationContact   연락처
 * @param experience            공연 경험
 * @param reason                신청 사유
 */
public record UserManagerRequestRequest(
        String organizationName,
        String organizationContact,
        String experience,
        String reason
) {
}
