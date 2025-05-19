package me.performancereservation.domain.admin.dto.response;

/**
 *
 * @param id                    // ManagerRequest ID
 * @param userId                // 공연 관리자 신청 사용자 ID
 * @param userName              // 공연 관리자 신청 사용자 이름
 * @param phoneNumber           // 공연 관리자 신청 사용자 전화번호
 * @param organizationName      // 공연 단체 혹은 개인 이름
 * @param organizationContact   // 공연 단체 연락처
 * @param experience            // 공연 경험
 * @param reason                // 공연 관리자 신청 사유
 */
public record PendingManagerRequestPageResponse(
        long id,
        long userId,
        String userName,
        String phoneNumber,
        String organizationName,
        String organizationContact,
        String experience,
        String reason
) {
}
