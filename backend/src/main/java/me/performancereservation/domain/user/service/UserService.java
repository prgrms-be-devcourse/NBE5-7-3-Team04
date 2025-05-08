package me.performancereservation.domain.user.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    //유저 정보 입력 (회원가입)
    @Transactional
    public User registerUser(String email, String name, String phoneNumber, Role role) {
        if(userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.USER_EMAIL_DUPLICATED, ErrorType.SERVICE); //중복 이메일은 예외 처리
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



    //email 기반 유저 조회
}
