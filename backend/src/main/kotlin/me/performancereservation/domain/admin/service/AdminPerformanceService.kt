package me.performancereservation.domain.admin.service

import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse
import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse
import me.performancereservation.domain.admin.mapper.AdminManagerRequestMapper
import me.performancereservation.domain.admin.mapper.AdminPerformanceMapper
import me.performancereservation.domain.admin.repository.AdminManagerRequestRepository
import me.performancereservation.domain.admin.repository.AdminPerformanceRepository
import me.performancereservation.domain.admin.repository.AdminPerformanceScheduleRepository
import me.performancereservation.domain.admin.repository.AdminUserRepository
import me.performancereservation.domain.file.FileRepository
import me.performancereservation.domain.performance.entities.Performance
import me.performancereservation.domain.performance.entities.PerformanceSchedule
import me.performancereservation.domain.performance.enums.PerformanceStatus
import me.performancereservation.domain.sms.SMSService
import me.performancereservation.domain.user.entitiy.ManagerRequest
import me.performancereservation.domain.user.entitiy.User
import me.performancereservation.domain.user.enums.ManagerRequestStatus
import me.performancereservation.domain.user.repository.UserRepository
import me.performancereservation.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.get

@Service
class AdminPerformanceService(
    private val smsService: SMSService,
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val adminUserRepository: AdminUserRepository,
    private val adminPerformanceRepository: AdminPerformanceRepository,
    private val adminManagerRequestRepository: AdminManagerRequestRepository,
    private val adminPerformanceScheduleRepository: AdminPerformanceScheduleRepository
) {

    /**
     * 어드민이 PENDING 상태의 공연 목록을 조회
     * PENDING 상태의 공연과 포스터, 스케줄, 공연 관리자 정보를 묶어 반환
     */
    @Transactional(readOnly = true)
    fun getPendingPerformanceList(pageable: Pageable, status: PerformanceStatus?): Page<PendingPerformancePageResponse> {
        // 페이징된 요청 받은 상태의 공연 조회
        val performances = if (status == null) {
            adminPerformanceRepository.findAllPerformance(pageable)
        } else {
            adminPerformanceRepository.findAllByStatusOrderByCreatedAt(status, pageable)
        }

        // 페이징된 공연의 파일 id 추출
        val fileIds = performances.content
            .mapNotNull { it.fileId }

        // 페이징된 공연의 매니저 id 추출
        val managerIds = performances.content
            .mapNotNull { it.managerId }

        // fileId로 조회한 경로 매핑
        val fileUrlMap = fileRepository.findAllById(fileIds)
            .associate { it.id!! to it.key }

        // managerId로 조회한 이름 매핑
        val managerNameMap = userRepository.findAllById(managerIds)
            .associate { it.id!! to it.name }

        // 공연 ID 목록 생성
        val performanceIds = performances.content.map { it.id!! }

        // 공연 ID에 해당하는 스케줄 목록 조회
        val scheduleMap = adminPerformanceScheduleRepository.findByPerformanceIdIn(performanceIds)
            .groupBy { it.performanceId }

        // 응답 페이징 반환
        return performances.map { performance ->
            convertToPerformanceResponse(performance, scheduleMap, fileUrlMap, managerNameMap)
        }
    }

    // Performance 엔티티를 PendingPerformancePageResponse 로 변환
    private fun convertToPerformanceResponse(
        performance: Performance,
        scheduleMap: Map<Long, List<PerformanceSchedule>>,
        fileUrlMap: Map<Long, String>,
        managerNameMap: Map<Long, String>
    ): PendingPerformancePageResponse {

        // 스케줄 응답 목록 생성
        val scheduleResponses = scheduleMap[performance.id!!]
            ?.map(AdminPerformanceMapper::toScheduleResponse)
            ?: emptyList()

        // Mapper를 사용하여 응답 객체 생성
        return AdminPerformanceMapper.toPendingResponse(
            performance = performance,
            fileUrl = fileUrlMap[performance.fileId],
            managerName = managerNameMap[performance.managerId] ?: "",
            schedules = scheduleResponses
        )
    }

    /**
     * 공연을 승인
     */
    @Transactional
    fun confirmPerformance(performanceId: Long) {
        val performance = adminPerformanceRepository.findByIdOrNull(performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")

        if (!performance.isPending) {
            throw ErrorCode.PERFORMANCE_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연만 승인 할 수 있습니다.")
        }

        // 요청자의 사용자 정보 조회
        val user = userRepository.findByIdOrNull(performance.managerId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 공연자를 찾을 수 없습니다.")

        // 공연 승인
        performance.confirm()

        // TODO 시연시 주석 제거
        // 공연 승인 완료 메시지 전송
        // smsService.performanceConfirmed(performance, user)
    }

    /**
     * 공연을 거부
     */
    @Transactional
    fun rejectPerformance(performanceId: Long) {
        val performance = adminPerformanceRepository.findByIdOrNull(performanceId)
            ?: throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=$performanceId")

        if (!performance.isPending) {
            throw ErrorCode.PERFORMANCE_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연만 승인, 거부 할 수 있습니다.")
        }

        // 요청자의 사용자 정보 조회
        val user = userRepository.findByIdOrNull(performance.managerId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 공연자를 찾을 수 없습니다.")

        // 공연 거부
        performance.reject()

        // TODO 시연시 주석 제거
        // 공연 거부 안내 문자
        // smsService.performanceRejected(performance, user)
    }

    /**
     * 어드민이 PENDING 상태의 매니저 요청 목록을 조회
     * PENDING 공연 관리자 요청과 해당하는 유저의 정보를 묶어 반환
     */
    @Transactional(readOnly = true)
    fun getPendingManagerRequestList(pageable: Pageable): Page<PendingManagerRequestPageResponse> {
        // 페이징된 PENDING 상태의 매니저 요청 조회
        val managerRequests = adminManagerRequestRepository.findAllByStatusOrderByCreatedAt(
            ManagerRequestStatus.PENDING,
            pageable
        )

        // 페이징된 매니저 요청의 사용자 id 추출
        val userIds = managerRequests.content
            .mapNotNull { it.userId }

        // 사용자 ID로 조회한 사용자 정보 매핑
        val userMap = adminUserRepository.findAllById(userIds)
            .associateBy { it.id!! }

        // 응답 페이징 반환
        return managerRequests.map { managerRequest ->
            convertToManagerRequestResponse(managerRequest, userMap)
        }
    }

    // ManagerRequest 엔티티를 PendingManagerRequestPageResponse 로 변환
    private fun convertToManagerRequestResponse(
        managerRequest: ManagerRequest,
        userMap: Map<Long, User> //TODO
    ): PendingManagerRequestPageResponse {

        // 사용자 정보 조회
        val user = userMap[managerRequest.userId]!!

        // Mapper를 사용하여 응답 객체 생성
        return AdminManagerRequestMapper.toPendingResponse(managerRequest, user)
    }

    /**
     * 공연 관리자 요청을 승인
     */
    @Transactional
    fun approveManagerRequest(managerRequestId: Long) {
        val managerRequest = adminManagerRequestRepository.findByIdOrNull(managerRequestId)
            ?: throw ErrorCode.MANAGER_REQUEST_NOT_FOUND.domainException("해당하는 공연 관리자 요청을 찾을 수 없습니다. id=$managerRequestId")

        if(!managerRequest.isPending) {
            throw ErrorCode.MANAGER_REQUEST_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연 관리자 요청만 승인, 거부가 가능합니다.")
        }

        // 요청자의 사용자 정보 조회
        val user = userRepository.findByIdOrNull(managerRequest.userId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다. id=${managerRequest.userId}")

        // 공연 관리자 요청 승인
        managerRequest.approve()

        // 사용자 ROLE 변경
        user.promoteManager()

        // TODO 시연시 주석 제거
        // 공연 관리자 승인 안내 문자
        // smsService.managerRequestApproved(user)
    }

    /**
     * 공연 관리자 요청을 거부
     */
    @Transactional
    fun rejectManagerRequest(managerRequestId: Long) {
        val managerRequest = adminManagerRequestRepository.findByIdOrNull(managerRequestId)
            ?: throw ErrorCode.MANAGER_REQUEST_NOT_FOUND.domainException("해당하는 공연 관리자 요청을 찾을 수 없습니다. id=$managerRequestId")

        if (!managerRequest.isPending) {
            throw ErrorCode.MANAGER_REQUEST_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연 관리자 요청만 승인, 거부가 가능합니다.")
        }

        // 요청자의 사용자 정보 조회
        val user = userRepository.findByIdOrNull(managerRequest.userId)
            ?: throw ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다.")

        // 공연 관리자 요청 거부
        managerRequest.reject()

        // TODO 시연시 주석 제거
        // 공연 관리자 거부 안내 문자
        // smsService.managerRequestRejected(user)
    }
}