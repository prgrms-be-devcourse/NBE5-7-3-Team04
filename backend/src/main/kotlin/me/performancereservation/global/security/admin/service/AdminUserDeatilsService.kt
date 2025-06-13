package me.performancereservation.global.security.admin.service

import me.performancereservation.domain.admin.repository.AdminRepository
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.security.admin.AdminUserDetails
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class AdminUserDetailsService(
    private val adminRepository: AdminRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val admin = adminRepository.findByIdOrNull(username)
            ?: throw ErrorCode.ADMIN_NOT_FOUND.domainException("해당하는 정보의 어드민이 없습니다")

        return AdminUserDetails(admin)
    }
}