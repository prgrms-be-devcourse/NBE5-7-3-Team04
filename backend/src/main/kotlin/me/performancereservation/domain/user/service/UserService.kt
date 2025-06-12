package me.performancereservation.domain.user.service

import me.performancereservation.domain.user.dto.request.UserManagerRequestRequest
import me.performancereservation.domain.user.dto.request.UserOnboardingRequest
import me.performancereservation.domain.user.entitiy.ManagerRequest
import me.performancereservation.domain.user.entitiy.User
import me.performancereservation.domain.user.enums.ManagerRequestStatus
import me.performancereservation.domain.user.enums.Role
import me.performancereservation.domain.user.repository.ManagerRequestRepository
import me.performancereservation.domain.user.repository.UserRepository
import me.performancereservation.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserTransactionalService(
    private val userRepository: UserRepository
) {
    //유저 정보 입력 (회원가입)
    @Transactional
    fun registerUser(email: String, name: String, phoneNumber: String, role: Role): User {
        if (userRepository.existsByEmail(email)) {
            throw ErrorCode.DUPLICATE_USER_EMAIL.serviceException() //중복 이메일은 예외 처리
        }
        val user = User(
            email = email,
            name = name,
            phoneNumber = phoneNumber,
            role = role
        )
        return userRepository.save(user)
    }
}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val managerRequestRepository: ManagerRequestRepository,
    private val userTransactionalService: UserTransactionalService
) {
    @Transactional
    fun onboard(userId: Long, request: UserOnboardingRequest) {
        val user = getUserById(userId)
        if (user.phoneNumber.isBlank()) {
            user.updatePhoneNumber(request.phoneNumber)
        }
        if (user.email.isBlank()) {
            user.updateEmail(request.email)
        }
        userRepository.save(user)
    }

    //id 기반 유저 조회
    fun getUserById(userId: Long): User {
        return userRepository.findByIdOrNull(userId)
            ?: throw ErrorCode.USER_NOT_FOUND.serviceException()
    }

    //email 기반 유저 조회
    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw ErrorCode.USER_NOT_FOUND.serviceException()
    }

    //테스트용 서비스
    fun createTestUserAndToken(email: String, name: String, phoneNumber: String, role: Role): User {
        return userRepository.findByEmail(email)
            ?: userTransactionalService.registerUser(email, name, phoneNumber, role)
    }

    /** 사용자가 공연 관리자 권한 신청
     *
     * @param userId
     */
    @Transactional
    fun submitManagerRequest(userId: Long, request: UserManagerRequestRequest) {
        if (!canRequestManagerRole(userId)) {
            throw ErrorCode.MANAGER_REQUEST_ALREADY_EXISTS.domainException("이미 공연자 권한 요청 중이거나 공연 관리자 입니다")
        }

        val managerRequest = ManagerRequest(
            userId = userId,
            reason = request.reason,
            experience = request.experience,
            organizationName = request.organizationName,
            organizationContact = request.organizationContact,
            status = ManagerRequestStatus.PENDING
        )

        managerRequestRepository.save(managerRequest)
    }

    /**
     * 매니저 권한 신청 가능 여부 확인
     * 이미 매니저이거나 신청 중인 요청이 있으면 신청 불가
     *
     * @param userId 사용자 ID
     * @return 신청 가능 여부
     */
    fun canRequestManagerRole(userId: Long): Boolean {
        // 이미 승인된 요청이 있는지 확인 (이미 매니저인 경우)
        if (managerRequestRepository.hasApprovedRequest(userId)) {
            return false
        }

        // 대기 중인 요청이 있는지 확인
        if (managerRequestRepository.hasPendingRequest(userId)) {
            return false
        }

        // 승인된 요청도 없고 대기 중인 요청도 없으면 신청 가능
        return true
    }
}
