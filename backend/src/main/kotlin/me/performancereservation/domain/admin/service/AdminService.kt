package me.performancereservation.domain.admin.service

import me.performancereservation.domain.user.repository.UserRepository
import me.performancereservation.global.exception.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminService(
    private val userRepository: UserRepository
) {

    /**
     * 세션을 받아 어드민 권한이 있는지 확인
     */
    fun checkAuthentication() {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth == null || !auth.isAuthenticated || auth.name == "anonymousUser") {
            throw ErrorCode.ADMIN_AUTHENTICATION_REQUIRED.domainException("어드민 인증이 필요합니다.")
        }
    }

    fun getUserName(userId: Long): String {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자가 없습니다.")
        return user.name
    }
}