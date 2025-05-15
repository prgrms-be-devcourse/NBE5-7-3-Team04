package me.performancereservation.domain.admin.dto.response;

/** 어드민 공연 관리자 승인 확인용 응답
 *
 * @param id            // ManagerRequest ID
 * @param userId        // 공연 관리자 신청 사용자 ID
 * @param userName      // 공연 관리자 신청 사용자 이름
 * @param phoneNumber   // 공연 관리자 신청 사용자 전화번호
 */
public record PendingManagerRequestPageResponse(
        long id,
        long userId,
        String userName,
        String phoneNumber
) {
}
