package me.performancereservation.domain.user.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.ManagerRequestRepository;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void submitManagerRequest(Long userId) {
        ManagerRequest managerRequest = ManagerRequest.builder()
                .userId(userId)
                .status(ManagerRequestStatus.PENDING)
                .build();

        managerRequestRepository.save(managerRequest);
    }
}
