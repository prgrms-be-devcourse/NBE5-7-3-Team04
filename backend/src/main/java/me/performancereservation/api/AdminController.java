package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.api.docs.AdminApiDocs;
import me.performancereservation.domain.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController implements AdminApiDocs {

    private final AdminService adminService;

    // 세션 기반 인증 상태 확인
    @GetMapping("/check-auth")
    public ResponseEntity<Void> checkAuth() {
        adminService.checkAuthentication();
        return ResponseEntity.ok().build();
    }

    // 권한 확인 테스트용 API
    @Override
    @GetMapping("/auth-test")
    public ResponseEntity<Object> authTest() {
        //인증을 가져와서 권한을 반환해 ADMIN 이 맞는지 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(auth.getAuthorities().stream().findFirst());
    }

    // csrf토큰 발급 API 프론트에서 어드민 관련 기능 사용시 여기서 토큰을 받아 같이 보내주는게 필요
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

}