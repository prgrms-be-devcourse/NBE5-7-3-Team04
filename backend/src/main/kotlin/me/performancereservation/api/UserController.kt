package me.performancereservation.api

import me.performancereservation.api.docs.UserApiDocs
import me.performancereservation.domain.user.dto.UserResponse
import me.performancereservation.domain.user.dto.request.UserManagerRequestRequest
import me.performancereservation.domain.user.dto.request.UserOnboardingRequest
import me.performancereservation.domain.user.service.UserService
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) : UserApiDocs {

    //http://localhost:8080/oauth2/authorization/google 로그인 테스트
    @GetMapping("/me")
    override fun getMyInfo(@AuthenticationPrincipal principal: CustomOAuth2User): ResponseEntity<UserResponse> {
        val user = principal.user
        return ResponseEntity.ok(UserResponse(user.id!!, user.email, user.name, user.phoneNumber, user.role))
    }

    @GetMapping("/manager-status")
    override fun canRequestManagerRole(@AuthenticationPrincipal principal: CustomOAuth2User): ResponseEntity<Boolean> {
        val canRequest = userService.canRequestManagerRole(principal.user.id!!)
        return ResponseEntity.ok(canRequest)
    }

    @PostMapping("/manager-request")
    override fun submitManagerRequest(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @RequestBody request: UserManagerRequestRequest
    ): ResponseEntity<Void> {
        userService.submitManagerRequest(principal.user.id!!, request)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/onboarding")
    override fun onboard(
        @AuthenticationPrincipal principal: CustomOAuth2User,
        @RequestBody request: UserOnboardingRequest
    ): ResponseEntity<UserResponse> {
        userService.onboard(principal.user.id!!, request)
        return ResponseEntity.noContent().build()
    }
}
