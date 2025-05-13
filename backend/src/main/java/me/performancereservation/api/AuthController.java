package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.security.jwt.JwtTokenProvider;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    //이메일로 회원가입 + 토큰 발급을 테스트
    @PostMapping("/signup-test")
    public ResponseEntity<String> testSignUp(
            @RequestParam String email, @RequestParam String name) {

        User user = userService.createTestUserAndToken(email, name, null, Role.USER);

        String jwt = jwtTokenProvider.createAccessToken(user);
        return ResponseEntity.ok(jwt);
    }

    //가입된 유저의 테스트용 jwt 발급
    @GetMapping("token-test")
    public ResponseEntity<String> getTestToken(@AuthenticationPrincipal CustomOAuth2User principal) {
        User user = principal.getUser();
        String jwt = jwtTokenProvider.createAccessToken(user);
        return ResponseEntity.ok(jwt);
    }
}
