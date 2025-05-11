package me.performancereservation.domain.user.controller;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.dto.UserMeResponse;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.exception.ErrorType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    //http://localhost:8080/oauth2/authorization/google 로그인 테스트

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMyInfo(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorType.DOMAIN);
        }
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(new UserMeResponse(user.getId(), user.getEmail(), user.getName(), user.getRole()));
    }
}
