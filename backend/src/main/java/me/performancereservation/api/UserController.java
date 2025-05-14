package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.dto.UserResponse;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal CustomOAuth2User principal) {
        User user = principal.getUser();
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole()));
    }
}