package me.performancereservation.api

import lombok.RequiredArgsConstructor
import me.performancereservation.api.docs.AdminApiDocs
import me.performancereservation.domain.admin.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminService: AdminService
) : AdminApiDocs {

    // 세션 기반 인증 상태 확인
    @GetMapping("/check-auth")
    override fun checkAuth(): ResponseEntity<Void> {
        adminService.checkAuthentication()
        return ResponseEntity.ok().build()
    }

    // 권한 확인 테스트용 API
    @GetMapping("/auth-test")
    override fun authTest(): ResponseEntity<Any> {
        //인증을 가져와서 권한을 반환해 ADMIN 이 맞는지 확인
        val auth = SecurityContextHolder.getContext().getAuthentication()
        return ResponseEntity.ok(auth.getAuthorities().stream().findFirst())
    }

    // csrf토큰 발급 API 프론트에서 어드민 관련 기능 사용시 여기서 토큰을 받아 같이 보내주는게 필요
    @GetMapping("/csrf")
    fun csrf(token: CsrfToken): CsrfToken {
        return token
    }
}