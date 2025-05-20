package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Auth API", description = "인증 관련 API")
public interface AuthApiDocs {

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰 (INVALID_REFRESH_TOKEN)")
    })
    ResponseEntity<String> reissue(
        @Parameter(description = "리프레시 토큰", required = true) String refreshToken
    );

    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    })
    ResponseEntity<Void> logout(
        @Parameter(description = "사용자 ID", required = true) Long userId
    );

    @Operation(summary = "테스트 회원가입", description = "테스트용 회원가입을 수행합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공")
    })
    ResponseEntity<String> testSignUp(
        @Parameter(description = "이메일", required = true) String email,
        @Parameter(description = "이름", required = true) String name
    );

    @Operation(summary = "테스트 토큰 발급", description = "테스트용 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)")
    })
    ResponseEntity<String> getTestToken(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User principal
    );
} 