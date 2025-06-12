package me.performancereservation.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.user.dto.request.UserManagerRequestRequest;
import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.dto.request.UserOnboardingRequest;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.ManagerRequestRepository;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ManagerRequestRepository managerRequestRepository;

    //유저 정보 입력 (회원가입)
    @Transactional
    public User registerUser(String email, String name, String phoneNumber, Role role) {
        if(userRepository.existsByEmail(email)) {
            throw ErrorCode.DUPLICATE_USER_EMAIL.serviceException(); //중복 이메일은 예외 처리
        }
        User user = User.builder()
                .email(email)
                .name(name)
                .phoneNumber(phoneNumber)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    //kakao에서 이메일을 제공해주지 않는 점을 활용해 provider로 구분할까 했지만
    //좀 더 포괄적으로 비어있으면 입력받도록 구현
    @Transactional
    public void onboard(Long userId, UserOnboardingRequest request) {
        User user = getUserById(userId);
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
            user.updatePhoneNumber(request.phoneNumber);
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            user.updateEmail(request.email);
        }
        userRepository.save(user);
    }

    //id 기반 유저 조회
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(ErrorCode.USER_NOT_FOUND::serviceException);
    }

    //email 기반 유저 조회
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(ErrorCode.USER_NOT_FOUND::serviceException);
    }

    //테스트용 서비스
    public User createTestUserAndToken(String email, String name, String phoneNumber, Role role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> registerUser(email, name, phoneNumber, role));
    }

    /** 사용자가 공연 관리자 권한 신청
     *
     * @param userId
     */
    @Transactional
    public void submitManagerRequest(Long userId, UserManagerRequestRequest request) {
        if(!canRequestManagerRole(userId)){
            throw ErrorCode.MANAGER_REQUEST_ALREADY_EXISTS.domainException("이미 공연자 권한 요청 중이거나 공연 관리자 입니다");
        }

        ManagerRequest managerRequest = ManagerRequest.builder()
                .userId(userId)
                .reason(request.reason)
                .experience(request.experience)
                .organizationName(request.organizationName)
                .organizationContact(request.organizationContact)
                .status(ManagerRequestStatus.PENDING)
                .build();

        managerRequestRepository.save(managerRequest);
    }

    /**
     * 매니저 권한 신청 가능 여부 확인
     * 이미 매니저이거나 신청 중인 요청이 있으면 신청 불가
     *
     * @param userId 사용자 ID
     * @return 신청 가능 여부
     */
    public boolean canRequestManagerRole(Long userId) {
        // 이미 승인된 요청이 있는지 확인 (이미 매니저인 경우)
        if (managerRequestRepository.hasApprovedRequest(userId)) {
            return false;
        }

        // 대기 중인 요청이 있는지 확인
        if (managerRequestRepository.hasPendingRequest(userId)) {
            return false;
        }

        // 승인된 요청도 없고 대기 중인 요청도 없으면 신청 가능
        return true;
    }
}
