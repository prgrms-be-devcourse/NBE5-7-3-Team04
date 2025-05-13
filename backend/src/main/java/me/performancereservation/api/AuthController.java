package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.exception.ErrorCode;
import me.performancereservation.global.security.jwt.JwtTokenProvider;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import me.performancereservation.global.storage.redis.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    //리프레쉬 토큰으로 엑세스 토큰 재발급 (리프레쉬가 유효할 때)
    @PostMapping("/reissue")
    public ResponseEntity<String> reissue(@RequestParam String refreshToken) { //리프레쉬 토큰에서 uerId 추출

        jwtTokenProvider.validateAccessToken(refreshToken);

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        String savedToken = refreshTokenService.getRefreshToken(userId);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw ErrorCode.INVALID_REFRESH_TOKEN.domainException("리프레쉬 토큰이 유효하지 않습니다.");
        }

        jwtTokenProvider.validateRefreshToken(refreshToken);

        User user = userService.getUserById(userId);
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        return ResponseEntity.ok(newAccessToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam Long userId) {
        refreshTokenService.deleteRefreshToken(userId);
        return ResponseEntity.noContent().build();
    }

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
