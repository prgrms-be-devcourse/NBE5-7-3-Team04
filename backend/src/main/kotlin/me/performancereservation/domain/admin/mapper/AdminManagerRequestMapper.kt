package me.performancereservation.domain.admin.mapper

import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse
import me.performancereservation.domain.user.entitiy.ManagerRequest
import me.performancereservation.domain.user.entitiy.User

object AdminManagerRequestMapper {
    /**
     * 관리자 매니저 요청 Pending 목록 페이지 응답용
     *
     * @param managerRequest 매니저 요청 엔티티
     * @param user 사용자 엔티티
     * @return PendingManagerRequestPageResponse
     */
    @JvmStatic  //TODO: 서비스 코틀린으로 변환시 @JvmStatic 제거 필요
    fun toPendingResponse(
        managerRequest: ManagerRequest,
        user: User
    ): PendingManagerRequestPageResponse {
        return PendingManagerRequestPageResponse(
            id = managerRequest.id,
            userId = user.id,
            userName = user.name,
            phoneNumber = user.phoneNumber,
            organizationName = managerRequest.organizationName,
            organizationContact = managerRequest.organizationContact,
            experience = managerRequest.experience,
            reason = managerRequest.reason
        )
    }
}