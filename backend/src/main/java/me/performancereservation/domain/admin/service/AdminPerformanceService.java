package me.performancereservation.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse;
import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse;
import me.performancereservation.domain.admin.dto.response.PendingPerformanceScheduleResponse;
import me.performancereservation.domain.admin.mapper.AdminManagerRequestMapper;
import me.performancereservation.domain.admin.mapper.AdminPerformanceMapper;
import me.performancereservation.domain.admin.repository.AdminManagerRequestRepository;
import me.performancereservation.domain.admin.repository.AdminPerformanceRepository;
import me.performancereservation.domain.admin.repository.AdminPerformanceScheduleRepository;
import me.performancereservation.domain.admin.repository.AdminUserRepository;
import me.performancereservation.domain.file.File;
import me.performancereservation.domain.file.FileRepository;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPerformanceService {

    private final AdminPerformanceRepository adminPerformanceRepository;
    private final AdminPerformanceScheduleRepository adminPerformanceScheduleRepository;
    private final AdminManagerRequestRepository adminManagerRequestRepository;
    private final AdminUserRepository adminUserRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    /** 어드민이 PENDING 상태의 공연 목록을 조회
     * PENDING 상태의 공연과 포스터, 스케줄, 공연 관리자 정보를 묶어 반환
     *
     * @param pageable 페이징 정보
     * @return Page<PendingPerformancePageResponse> 페이징된 Pending 공연 목록
     */
    @Transactional(readOnly = true)
    public Page<PendingPerformancePageResponse> getPendingPerformanceList(Pageable pageable) {
        // 페이징된 PENDING 상태의 공연 조회
        Page<Performance> performances = adminPerformanceRepository.findAllByStatusOrderByCreatedAt(PerformanceStatus.PENDING, pageable);

        // 페이징된 공연의 파일 id 추출
        List<Long> fileIds = performances.getContent().stream()
                .map(Performance::getFileId)
                .filter(Objects::nonNull)
                .toList();

        // 페이징된 공연의 매니저 id 추출
        List<Long> managerIds = performances.getContent().stream()
                .map(Performance::getManagerId)
                .filter(Objects::nonNull)
                .toList();

        // fileId로 조회한 경로 매핑
        Map<Long, String> fileUrlMap = fileRepository.findAllById(fileIds).stream()
                .collect(Collectors.toMap(File::getId, File::getKey));

        // managerId로 조회한 이름 매핑
        Map<Long, String> managerNameMap = userRepository.findAllById(managerIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        // 공연 ID 목록 생성
        List<Long> performanceIds = performances.getContent().stream()
                .map(Performance::getId)
                .toList();

        // 공연 ID에 해당하는 스케줄 목록 조회
        Map<Long, List<PerformanceSchedule>> scheduleMap = adminPerformanceScheduleRepository.findByPerformanceIdIn(performanceIds)
                .stream()
                .collect(Collectors.groupingBy(PerformanceSchedule::getPerformanceId));

        // 응답 페이징 반환
        return performances.map(performance -> convertToPerformanceResponse(performance, scheduleMap, fileUrlMap, managerNameMap));
    }


    // Performance 엔티티를 PendingPerformancePageResponse 로 변환
    private PendingPerformancePageResponse convertToPerformanceResponse(
            Performance performance,
            Map<Long, List<PerformanceSchedule>> scheduleMap,
            Map<Long, String> fileUrlMap,
            Map<Long, String> managerNameMap) {

        // 스케줄 응답 목록 생성
        List<PendingPerformanceScheduleResponse> scheduleResponses = scheduleMap
                .getOrDefault(performance.getId(), List.of())
                .stream()
                .map(AdminPerformanceMapper::toScheduleResponse)
                .collect(Collectors.toList());

        // Mapper를 사용하여 응답 객체 생성
        return AdminPerformanceMapper.toPendingResponse(
                performance,
                fileUrlMap.get(performance.getFileId()),
                managerNameMap.get(performance.getManagerId()),
                scheduleResponses
        );
    }



    /** 공연을 승인
     *
     * @param performanceId
     */
    @Transactional
    public void confirmPerformance(Long performanceId) {
        Performance performance = adminPerformanceRepository.findById(performanceId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + performanceId));

        if(!performance.isPending()){
            throw ErrorCode.PERFORMANCE_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연만 승인 할 수 있습니다.");
        }

        // 공연 승인
        performance.confirm();
    }

    /** 공연을 거부
     *
     * @param performanceId
     */
    @Transactional
    public void rejectPerformance(Long performanceId) {
        Performance performance = adminPerformanceRepository.findById(performanceId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + performanceId));

        if(!performance.isPending()){
            throw ErrorCode.PERFORMANCE_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연만 승인, 거부 할 수 있습니다.");
        }

        // 공연 거부
        performance.reject();
    }

    /** 어드민이 PENDING 상태의 매니저 요청 목록을 조회
     * PENDING 공연 관리자 요청과 해당하는 유저의 정보를 묶어 반환
     *
     * @param pageable 페이징 정보
     * @return Page<PendingManagerRequestPageResponse> 페이징된 Pending 매니저 요청 목록
     */
    @Transactional(readOnly = true)
    public Page<PendingManagerRequestPageResponse> getPendingManagerRequestList(Pageable pageable) {
        // 페이징된 PENDING 상태의 매니저 요청 조회
        Page<ManagerRequest> managerRequests = adminManagerRequestRepository.findAllByStatusOrderByCreatedAt(ManagerRequestStatus.PENDING, pageable);

        // 페이징된 매니저 요청의 사용자 id 추출
        List<Long> userIds = managerRequests.getContent().stream()
                .map(ManagerRequest::getUserId)
                .filter(Objects::nonNull)
                .toList();

        // 사용자 ID로 조회한 사용자 정보 매핑
        Map<Long, User> userMap = adminUserRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 응답 페이징 반환
        return managerRequests.map(managerRequest -> convertToManagerRequestResponse(managerRequest, userMap));
    }


    // ManagerRequest 엔티티를 PendingManagerRequestPageResponse 로 변환
    private PendingManagerRequestPageResponse convertToManagerRequestResponse(
            ManagerRequest managerRequest,
            Map<Long, User> userMap) {

        // 사용자 정보 조회
        User user = userMap.get(managerRequest.getUserId());

        // Mapper를 사용하여 응답 객체 생성
        return AdminManagerRequestMapper.toPendingResponse(managerRequest, user);
    }

    /** 공연 관리자 요청을 승인
     *
     * @param managerRequestId
     */
    @Transactional
    public void approveManagerRequest(Long managerRequestId) {
        ManagerRequest managerRequest = adminManagerRequestRepository.findById(managerRequestId)
                .orElseThrow(() -> ErrorCode.MANAGER_REQUEST_NOT_FOUND.domainException("해당하는 공연 관리자 요청을 찾을 수 없습니다. id=" + managerRequestId));

        if(!managerRequest.isPending()) {
            throw ErrorCode.MANAGER_REQUEST_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연 관리자 요청만 승인, 거부가 가능합니다.");
        }

        // 요청자의 사용자 정보 조회
        User user = userRepository.findById(managerRequest.getUserId())
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.domainException("해당하는 사용자를 찾을 수 없습니다. id=" + managerRequest.getUserId()));

        // 공연 관리자 요청 승인
        managerRequest.approve();

        // 사용자 ROLE 변경
        user.promoteManager();
    }

    /** 공연 관리자 요청을 거부
     *
     * @param managerRequestId
     */
    @Transactional
    public void rejectManagerRequest(Long managerRequestId) {
        ManagerRequest managerRequest = adminManagerRequestRepository.findById(managerRequestId)
                .orElseThrow(() -> ErrorCode.MANAGER_REQUEST_NOT_FOUND.domainException("해당하는 공연 관리자 요청을 찾을 수 없습니다. id=" + managerRequestId));

        if(!managerRequest.isPending()) {
            throw ErrorCode.MANAGER_REQUEST_STATUS_NOT_PENDING.domainException("PENDING 상태의 공연 관리자 요청만 승인, 거부가 가능합니다.");
        }

        //공연 관리자 요청 거부
        managerRequest.reject();
    }
}