package me.performancereservation.domain.admin.mapper;

import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse;
import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.entitiy.User;

public class AdminManagerRequestMapper {

    /**
     * 관리자 매니저 요청 Pending 목록 페이지 응답용
     *
     * @param managerRequest 매니저 요청 엔티티
     * @param user 사용자 엔티티
     * @return PendingManagerRequestPageResponse
     */
    public static PendingManagerRequestPageResponse toPendingResponse(
            ManagerRequest managerRequest,
            User user) {

        return new PendingManagerRequestPageResponse(
                managerRequest.getId(),
                user.getId(),
                user.getName(),
                user.getPhoneNumber()
        );
    }
}