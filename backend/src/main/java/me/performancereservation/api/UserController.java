package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.api.docs.UserApiDocs;
import me.performancereservation.domain.user.dto.request.UserManagerRequestRequest;
import me.performancereservation.domain.user.dto.request.UserOnboardingRequest;
import me.performancereservation.domain.user.dto.UserResponse;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.service.UserService;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApiDocs {

    private final UserService userService;
    //http://localhost:8080/oauth2/authorization/google 로그인 테스트

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal CustomOAuth2User principal) {
        User user = principal.getUser();
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getPhoneNumber() ,user.getRole()));
    }

    @Override
    @GetMapping("/manager-status")
    public ResponseEntity<Boolean> canRequestManagerRole(@AuthenticationPrincipal CustomOAuth2User principal) {
        boolean canRequest = userService.canRequestManagerRole(principal.getUser().getId());
        return ResponseEntity.ok(canRequest);
    }

    @Override
    @PostMapping("/manager-request")
    public ResponseEntity<Void> submitManagerRequest(@AuthenticationPrincipal CustomOAuth2User principal,
                                                     @RequestBody UserManagerRequestRequest request) {
        userService.submitManagerRequest(principal.getUser().getId(), request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/onboarding")
    public ResponseEntity<UserResponse> onboard(@AuthenticationPrincipal CustomOAuth2User principal,
                                                @RequestBody UserOnboardingRequest request) {
        userService.onboard(principal.getUser().getId(), request);
        return ResponseEntity.noContent().build();
    }


//     // TODO: 테스트 완료 후 삭제
//     private final UserRepository userRepository;

//     @Transactional
//     @PatchMapping("/approve/me")
//     public ResponseEntity<Void> approveMe(@AuthenticationPrincipal CustomOAuth2User principal) {
//         User userPrincipal = principal.getUser();
//         Long userid = userPrincipal.getId();

//         // 요청자의 사용자 정보 조회
//         userRepository.updateUserRole(userid, Role.MANAGER);
//         // 사용자 ROLE 변경
// //        user.promoteManager();
//         return ResponseEntity.noContent().build();
//     }
}
